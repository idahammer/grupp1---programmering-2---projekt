package com.soundcloud.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.io.AbstractSessionInputBuffer;
import org.apache.http.impl.io.HttpResponseParser;
import org.apache.http.message.BasicLineParser;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StreamTest {
    @Test
    public void testParsing() throws Exception {
        Stream s = new Stream(
                "http://api.soundcloud.com",
                "http://ak-media.soundcloud.com/Nbhil06qjDaP.128.mp3?AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1319537336&Signature=tzk9EAm3bcjpMJ0cukHPdVx2ml4%3D&__gda__=1319537336_9354e7fea41da4f7a87e78db9a4ed582",
                parse("s3-headers.txt"));

        assertThat("etag", s.eTag, equalTo("\"81c4a04a366ab681ea068b2fa06d10a3\""));
        assertThat("bitrate", s.bitRate, is(128));
        assertThat("duration", s.duration, is(18998));
        assertThat("content-length", s.contentLength, is(303855L));
        assertThat("last-modified", s.lastModified, is(1319536883000L));
        assertThat("expires", s.expires, is(1319537336000L));
    }

    @Test
    public void testWithNewStreamUrl() throws Exception {
        Stream s1 = new Stream(
                     "http://api.soundcloud.com",
                     "http://ak-media.soundcloud.com/Nbhil06qjDaP.128.mp3?AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1319537336&Signature=tzk9EAm3bcjpMJ0cukHPdVx2ml4%3D&__gda__=1319537336_9354e7fea41da4f7a87e78db9a4ed582",
                     parse("s3-headers.txt"));

        Stream s2 = s1.withNewStreamUrl("http://ak-media.soundcloud.com/Nbhil06qjDaP.128.mp3?AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1319537337&Signature=tzk9EAm3bcjpMJ0cukHPdVx2ml4%3D&__gda__=1319537336_9354e7fea41da4f7a87e78db9a4ed582");

        assertThat(s1.eTag, equalTo(s2.eTag));
        assertThat(s1.bitRate, equalTo(s2.bitRate));
        assertThat(s1.duration, equalTo(s2.duration));
        assertThat(s1.contentLength, equalTo(s2.contentLength));
        assertThat(s1.lastModified, equalTo(s2.lastModified));

        assertThat(s1.streamUrl, not(equalTo(s2.streamUrl)));
        assertThat(s1.expires, not(is(s2.expires)));
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        Stream s1 = new Stream(
                "http://api.soundcloud.com",
                "http://ak-media.soundcloud.com/Nbhil06qjDaP.128.mp3?AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1319537336&Signature=tzk9EAm3bcjpMJ0cukHPdVx2ml4%3D&__gda__=1319537336_9354e7fea41da4f7a87e78db9a4ed582",
                parse("s3-headers.txt"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(s1);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Stream s2 = (Stream) ois.readObject();

        assertThat(s1.eTag, equalTo(s2.eTag));
        assertThat(s1.bitRate, equalTo(s2.bitRate));
        assertThat(s1.duration, equalTo(s2.duration));
        assertThat(s1.contentLength, equalTo(s2.contentLength));
        assertThat(s1.lastModified, equalTo(s2.lastModified));
        assertThat(s1.streamUrl, equalTo(s2.streamUrl));
        assertThat(s1.expires, equalTo(s2.expires));
    }

    private HttpResponse parse(final String resource) throws IOException, HttpException {
        final HttpParams params = new BasicHttpParams();
        HttpResponseParser parser = new HttpResponseParser(new AbstractSessionInputBuffer() {
            {
                init(getClass().getResourceAsStream(resource), 8192, params);
            }

            @Override
            public boolean isDataAvailable(int timeout) throws IOException {
                return true;
            }
        }, new BasicLineParser(), new DefaultHttpResponseFactory(), params);

        return (HttpResponse) parser.parse();
    }
}
