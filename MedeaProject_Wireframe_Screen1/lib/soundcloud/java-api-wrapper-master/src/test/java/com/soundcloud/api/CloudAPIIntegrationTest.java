package com.soundcloud.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CloudAPIIntegrationTest implements Params.Track, Endpoints {
    // https://soundcloud.com/you/apps/java-api-wrapper
    // user: api-testing
    static final String CLIENT_ID     = "40d3111c6b4d02096c6ce35fdf90bf58";
    static final String CLIENT_SECRET = "ff3685dbf02ce789a16631b0028e0512";

    public static final String TRACK_PERMALINK = "http://soundcloud.com/jberkel/nobody-home";
    public static final String MEDIA_LINK = "http://media.soundcloud.com/stream/zkwlN5MGNsJt";
    public static final long USER_ID      = 18173653L;
    public static final long CHE_FLUTE_TRACK_ID = 274334;
    public static final long FLICKERMOOD_TRACK_ID = 293;
    public static final long TRACK_LENGTH = 224861L;

    CloudAPI api;

    static final String USERNAME = "android-testing";
    static final String PASSWORD = "android-testing";

    /*
    To get full HTTP logging, add the following system properties:
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
    -Dorg.apache.commons.logging.simplelog.showdatetime=true
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR
    */

    @Rule
    public Retry retryRule = new Retry(3);

    @Before
    public void setUp() throws Exception {
        api = new ApiWrapper(
                CLIENT_ID,
                CLIENT_SECRET,
                null,
                null);

    }

    private Token login(String... scopes) throws IOException {
        return api.login(USERNAME, PASSWORD, scopes);
    }

    @Test
    public void shouldBeAbleToMakePublicRequests() throws Exception {
        HttpResponse response = api.get(Request.to("/tracks").with("order", "hotness"));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldUploadASimpleAudioFile() throws Exception {
        login();
        HttpResponse resp = api.post(Request.to(TRACKS).with(
                TITLE, "Hello Android",
                POST_TO_EMPTY, "")
                .withFile(ASSET_DATA, new File(getClass().getResource("hello.aiff").getFile())));

        int status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(201));

        Header location = resp.getFirstHeader("Location");
        assertNotNull(location);
    }

    @Test
    public void shouldCreateAPlaylistAndAddTracksToIt() throws Exception {
        login();

        HttpResponse resp = api.post(Request.to(PLAYLISTS)
                .with("playlist[title]", "test playlist"));

        int status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(201));

        Header location = resp.getFirstHeader("Location");
        assertNotNull(location);

        String playlistUrl = location.getValue();
        assertNotNull(playlistUrl);

        String title = "a new title:" + System.currentTimeMillis();
        resp = api.put(Request.to(playlistUrl)
                .with("playlist[title]", title)
                .with("playlist[tracks][][id]", CHE_FLUTE_TRACK_ID)
                .with("playlist[tracks][][id]", FLICKERMOOD_TRACK_ID));

        status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(200));

        JSONObject obj = new JSONObject(EntityUtils.toString(resp.getEntity()));
        assertThat(obj.getString("kind"), equalTo("playlist"));
        assertThat(obj.getString("title"), equalTo(title));
        assertThat(obj.getInt("track_count"), equalTo(2));
    }

    @Test
    public void shouldCreateAPlaylistAndAddTracksToItWithJSON() throws Exception {
        login();

        HttpResponse resp = api.post(Request.to(PLAYLISTS)
                .with("playlist[title]", "test playlist"));

        int status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(201));

        Header location = resp.getFirstHeader("Location");
        assertNotNull(location);

        String playlistUrl = location.getValue();
        assertNotNull(playlistUrl);

        String title = "a new tîtle:" + System.currentTimeMillis();
        JSONObject json = createJSONPlaylist(title, CHE_FLUTE_TRACK_ID, FLICKERMOOD_TRACK_ID);

        resp = api.put(Request.to(playlistUrl)
                .withContent(json.toString(), "application/json"));

        status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(200));

        JSONObject obj = new JSONObject(EntityUtils.toString(resp.getEntity()));
        assertThat(obj.getString("kind"), equalTo("playlist"));
        assertThat(obj.getString("title"), equalTo(title));
        assertThat(obj.getInt("track_count"), equalTo(2));
    }

    @Test
    public void shouldUploadASimpleAudioFileBytes() throws Exception {
        login();

        File f = new File(getClass().getResource("hello.aiff").getFile());
        ByteBuffer bb = ByteBuffer.allocate((int) f.length());
        FileInputStream fis = new FileInputStream(f);
        for (;;) if (fis.getChannel().read(bb) <= 0) break;

        HttpResponse resp = api.post(Request.to(TRACKS).with(
                  TITLE, "Hello Android",
                  POST_TO_EMPTY, "")
                .withFile(ASSET_DATA, bb, "hello.aiff"));

        int status = resp.getStatusLine().getStatusCode();
        assertThat(status, is(201));
    }


    @Test(expected = IOException.class) @Ignore
    public void shouldNotGetASignupTokenWhenInofficialApp() throws Exception {
        login();
        api.clientCredentials();
    }

    @Test(expected = CloudAPI.InvalidTokenException.class)
    public void shouldGetATokenUsingExtensionGrantTypes() throws Exception {
        // TODO ?
        api.extensionGrantType(CloudAPI.FACEBOOK_GRANT_TYPE + "fbToken");
    }

    @Test
    public void shouldReturn401WithInvalidToken() throws Exception {
        login();
        api.setToken(new Token("invalid", "invalid"));
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(401));
    }


    @Test
    public void shouldWorkWithRelativeUrls() throws Exception {
        login();
        HttpResponse resp = api.get(Request.to("me"));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldRefreshAutomaticallyWhenTokenExpired() throws Exception {
        login();

        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));

        final Token oldToken = api.getToken();

        assertThat(api.invalidateToken(), is(nullValue()));

        resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
        // make sure we've got a new token
        assertThat(oldToken, not(equalTo(api.getToken())));
    }

    @Test
    public void shouldResolveUrls() throws Exception {
        login();

        long id = api.resolve("http://soundcloud.com/" + USERNAME);
        assertThat(id, is(USER_ID));

        try {
            id = api.resolve("http://soundcloud.com/i-do-no-exist-no-no-no");
            fail("expected resolver exception, got: "+id);
        } catch (CloudAPI.ResolverException e) {
            // expected
            assertThat(e.getStatusCode(), is(404));
        }
    }

    @Test
    public void shouldResolveStreamUrls() throws Exception {
        login();

        String streamUrl = getApiUrlFromPermalink(TRACK_PERMALINK) + "/stream";
        Stream resolved = api.resolveStreamUrl(streamUrl, false);

        assertThat(resolved.url, equalTo(streamUrl));
        assertThat(resolved.streamUrl, containsString("https://ec-media.soundcloud.com/"));

        assertTrue("expire should be in the future", resolved.expires > System.currentTimeMillis());
        assertThat(resolved.eTag, equalTo("\"980f61d6d6ee26ffe0c78aef618d786f\""));
    }

    @Test
    public void shouldResolveNonApiStreamUrls() throws Exception {
        testResolveNonApiStreamUrls();
    }

    @Test
    public void shouldResolveNonApiStreamUrlsWithLogin() throws Exception {
        login();
        testResolveNonApiStreamUrls();
    }

    private void testResolveNonApiStreamUrls() throws IOException {
        Stream resolved = api.resolveStreamUrl(MEDIA_LINK, false);

        assertThat(resolved.url, equalTo(MEDIA_LINK));
        assertThat(resolved.streamUrl, containsString("http://ec-media.soundcloud.com/"));

        assertTrue("expire should be in the future", resolved.expires > System.currentTimeMillis());
        assertThat(resolved.eTag, equalTo("\"5eeb63b73f99ff2de44a60441d421d2a\""));
    }

    @Test
    public void shouldThrowResolverExceptionWhenStreamCannotBeResolved() throws Exception {
        login();
        try {
            Stream s = api.resolveStreamUrl("https://api.soundcloud.com/tracks/999919191/stream", false);
            fail("expected resolver exception, got: "+s);
        } catch (CloudAPI.ResolverException e) {
            // expected
            assertThat(e.getStatusCode(), is(404));
        }
    }

    @Test
    public void shouldSupportRangeRequest() throws Exception {
        login();

        String streamUrl = getApiUrlFromPermalink(TRACK_PERMALINK)+"/stream";

        Stream resolved = api.resolveStreamUrl(streamUrl, false);
        assertThat(resolved.contentLength, is(TRACK_LENGTH));

        HttpResponse resp = api
                .getHttpClient()
                .execute(resolved.streamUrl().range(50, 100).buildRequest(HttpGet.class));

        assertThat(resp.getStatusLine().toString(), resp.getStatusLine().getStatusCode(), is(206));
        Header range = resp.getFirstHeader("Content-Range");
        assertThat(range, notNullValue());
        assertThat(range.getValue(), equalTo("bytes 50-100/"+TRACK_LENGTH));
        assertThat(resp.getEntity().getContentLength(), is(51L));
    }

    @Test
    public void readMyDetails() throws Exception {
        login();

        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));

        assertThat(
                resp.getFirstHeader("Content-Type").getValue(),
                containsString("application/json"));

        JSONObject me = Http.getJSON(resp);

        assertThat(me.getString("username"), equalTo(USERNAME));
        // writeResponse(resp, "me.json");
    }

    @Test
    public void readGzipCompressedData() throws Exception {
        api.setDefaultAcceptEncoding("gzip");

        login();

        HttpResponse resp = api.get(Request.to(Endpoints.TRACKS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));

        assertThat(
                resp.getFirstHeader("Content-Type").getValue(),
                containsString("application/json"));

        assertThat(
                resp.getFirstHeader("Content-Encoding").getValue(),
                equalTo("gzip"));

        JSONArray array = new JSONArray(new JSONTokener(new InputStreamReader(resp.getEntity().getContent())));

        assertTrue("array is empty", array.length() > 0);
    }

    @Test
    public void shouldLoginWithNonExpiringScope() throws Exception {
        Token token = login(Token.SCOPE_NON_EXPIRING);
        assertThat(token.scoped(Token.SCOPE_NON_EXPIRING), is(true));
        assertThat(token.refresh, is(nullValue()));
        assertThat(token.getExpiresIn(), is(nullValue()));
        assertThat(token.valid(), is(true));

        // make sure we can issue a request with this token
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldNotRefreshWithNonExpiringScope() throws Exception {
        Token token = login(Token.SCOPE_NON_EXPIRING);
        assertThat(token.scoped(Token.SCOPE_NON_EXPIRING), is(true));
        assertThat(api.invalidateToken(), is(nullValue()));
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(401));
    }

    @Test
    public void shouldChangeContentType() throws Exception {
        login();

        api.setDefaultContentType("application/xml");
        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));

        assertThat(
                resp.getFirstHeader("Content-Type").getValue(),
                containsString("application/xml"));
    }


    @Test
    public void shouldSupportConditionalGets() throws Exception {
        login();

        HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
        assertThat(resp.getStatusLine().getStatusCode(), is(200) /* ok */);
        String etag = Http.etag(resp);
        assertNotNull(etag);

        resp = api.get(Request.to(Endpoints.MY_DETAILS).ifNoneMatch(etag));
        assertThat(resp.getStatusLine().getStatusCode(), is(304) /* not-modified */);
    }

    @Test(expected = UnknownHostException.class)
    public void shouldRespectProxySettings() throws Exception {
        System.setProperty("http.proxyHost", "http://doesnotexist.example.com");
        try {
            login();
        } finally {
            System.clearProperty("http.proxyHost");
        }
    }

    @Test @Ignore
    public void shouldSupportConcurrentConnectionsToApiHost() throws Exception {
        login();

        int num = 20;
        final CyclicBarrier start = new CyclicBarrier(num, new Runnable() {
            @Override
            public void run() {
                System.err.println("starting...");
            }
        });
        final CyclicBarrier end = new CyclicBarrier(num);
        while (num-- > 0) {
            new Thread("t-"+num) {
                @Override public void run() {
                    try {
                        start.await();
                        System.err.println("running: "+toString());
                        try {
                            HttpResponse resp = api.get(Request.to(Endpoints.MY_DETAILS));
                            resp.getEntity().consumeContent();
                            assertThat(resp.getStatusLine().getStatusCode(), is(200));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            System.err.println("finished: "+toString());
                            end.await();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
        start.await();
        end.await();
        System.err.println("all threads finished");
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void writeResponse(HttpResponse resp, String file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        InputStream is = resp.getEntity().getContent();
        byte[] b = new byte[8192];
        int n;

        while ((n = is.read(b)) >= 0) fos.write(b, 0, n);
        is.close();
        fos.close();
    }

    private String getApiUrlFromPermalink(String permalink) throws IOException {
        long trackId = api.resolve(permalink);
        return  "https://api.soundcloud.com/tracks/" + trackId;
    }


    private JSONObject createJSONPlaylist(String title, long... trackIds) throws JSONException {
        JSONObject playlist = new JSONObject();
        playlist.put("title", title);

        JSONObject json = new JSONObject();
        json.put("playlist", playlist);

        JSONArray tracks = new JSONArray();
        playlist.put("tracks", tracks);

        for (long id : trackIds) {
            JSONObject track = new JSONObject();
            track.put("id", id);
            tracks.put(track);
        }
        return json;
    }

    // We had trouble with some tests randomly failing, perhaps due to replication lag
    // see http://stackoverflow.com/questions/8295100/how-to-re-run-failed-junit-tests-immediately
    public static class Retry implements TestRule {
        private int retryCount;

        public Retry(int retryCount) {
            this.retryCount = retryCount;
        }

        public Statement apply(Statement base, Description description) {
            return statement(base, description);
        }

        private Statement statement(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Throwable caughtThrowable = null;

                    // implement retry logic here
                    for (int i = 0; i < retryCount; i++) {
                        try {
                            base.evaluate();
                            return;
                        } catch (Throwable t) {
                            caughtThrowable = t;
                            System.err.println(description.getDisplayName() + ": run " + (i+1) + " failed");
                        }
                    }
                    System.err.println(description.getDisplayName() + ": giving up after " + retryCount + " failures");
                    throw caughtThrowable;
                }
            };
        }
    }
}
