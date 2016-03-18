package com.soundcloud.api.examples;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.CloudAPI;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Token;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * This example shows how to get an API token by logging in w/ Facebook.
 */
public class FacebookConnect {
    // http://sandbox-soundcloud.com/you/apps/java-api-wrapper-test-app
    // user: api-testing

    //https://skitch.com/jberkel/ggb18/edit-java-api-wrapper-test-app-on-soundcloud-create-record-and-share-your-sounds-for-free
    static final String CLIENT_ID     = "yH1Jv2C5fhIbZfGTpKtujQ";
    static final String CLIENT_SECRET = "C6o8jc517b6PIw0RKtcfQsbOK3BjGpxWFLg977UiguY";
    static final URI REDIRECT_URI     = URI.create("http://localhost:8000");

    public static void main(String[] args) throws IOException {
        final ApiWrapper wrapper = new ApiWrapper(
                CLIENT_ID,
                CLIENT_SECRET,
                REDIRECT_URI,
                null    /* token */);


        // generate the URL the user needs to open in the browser
        URI url = wrapper.authorizationCodeUrl(Endpoints.FACEBOOK_CONNECT, Token.SCOPE_NON_EXPIRING);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(url);
        } else {
            System.err.println("open \"" + url + "\" in a browser");
        }

        // start a web server to get the redirect information
        startServer(wrapper);

        // note: on Android you would use a WebView instead and override 'shouldOverrideUrlLoading':

        /*
            WebView webView = (WebView) findViewById(R.id.webview);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                    if (url.startsWith(REDIRECT_URI)) {
                        Uri result = Uri.parse(url);
                        String error = result.getQueryParameter("error");
                        String code = result.getQueryParameter("code");
                    }
                    return true;
                }
            });

            webView.loadUrl(wrapper.authorizationCodeUrl(Endpoints.FACEBOOK_CONNECT, ...);
        */
    }

    static void startServer(ApiWrapper wrapper) throws IOException {
        ServerSocket socket = new ServerSocket(8000);
        for (;;) {
            final Socket client = socket.accept();
            try {
                InputStream is = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
                PrintStream out = new PrintStream(client.getOutputStream());
                String line = reader.readLine();
                if (line == null) throw new IOException("client closed connection without a request.");

                final String[] request = line.split(" ", 3);
                if (request.length != 3) throw new IOException("invalid request:" + line);
                if (!"GET".equals(request[0])) throw new IOException("invalid method:" + line);

                Map<String, String> params = parseParameters(request[1]);

                if (params.containsKey("error")) {
                    // error logging in, redirect mismatch etc.

                    reply(out, "Error: " + params.get("error_description"));
                } else if (params.containsKey("code")) {
                    // we got a code back, try to exchange it for a token
                    try {
                        Token token = wrapper.authorizationCode(params.get("code"));
                        reply(out, "Got token: " + token);
                    } catch (CloudAPI.InvalidTokenException e) {
                        reply(out, e.getMessage());
                    }
                } else {
                    // unexpected redirect
                    reply(out, "invalid request:"+request[1]);
                }
               break;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    static void reply(PrintStream out, String text) {
        System.out.println(text);

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain");
        out.println();
        out.println(text);
        out.flush();
    }

    static Map<String, String> parseParameters(String request) {
        Map<String, String> params = new HashMap<String, String>();
        if (request.contains("?")) {
            String query = request.substring(Math.min(request.length(), request.indexOf("?") + 1),
                    request.length());
            for (String s : query.split("&")) {
                String[] kv = s.split("=", 2);
                if (kv != null && kv.length == 2) {
                    try {
                        params.put(URLDecoder.decode(kv[0], "UTF-8"),
                                URLDecoder.decode(kv[1], "UTF-8"));
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }
        return params;
    }
}
