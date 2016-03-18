package com.soundcloud.api;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AUTH;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RequestTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentForNonEvenParams() throws Exception {
        new Request().with("1", 2, "3");
    }

    @Test
    public void shouldBuildAQueryString() throws Exception {
        assertThat(
                new Request().with("foo", 100, "baz", 22.3f, "met\u00f8l", false).queryString(),
                equalTo("foo=100&baz=22.3&met%C3%B8l=false"));
    }

    @Test
    public void shouldGenerateUrlWithParameters() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        assertThat(p.toUrl("http://foo.com"), equalTo("http://foo.com?foo=100&baz=22.3"));
    }


    @Test
    public void shouldNotModifyOriginalRequest() throws Exception {
        String url = "http://ec-media.soundcloud.com/SdPniMt7cZzj.128.mp3?ff61182e3c2ecefa438cd02102d0e385713f0c1f" +
                "af3b0339595660fd0603ed1dd95c308fdf4dfe37b272d5fc302cd60875f62fda2557f961990ca6e770fdb81c291f729" +
                "2cb&AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1337966965&Signature=dFluZNnDMGZiXCACfRru9VrB%2" +
                "Bbg%3D";

        Request r = new Request(url);
        assertThat(r.toUrl(), equalTo(url));
    }

    @Test
    public void shouldHaveSizeMethod() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        assertThat(p.size(), is(2));
    }

    @Test
    public void shouldSupportWith() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        p.add("baz", 66);
        assertThat(p.size(), is(3));
        assertThat(p.queryString(), equalTo("foo=100&baz=22.3&baz=66"));
    }

    @Test
    public void shouldSupportOverwritingParameters() {
        Request r = new Request();
        r.add("foo", 1)
         .add("foo", 2);

        assertThat(r.queryString(), equalTo("foo=1&foo=2"));

        r.set("foo", 3);
        assertThat(r.queryString(), equalTo("foo=3"));

        r.clear("foo");
        assertThat(r.queryString(), equalTo(""));
    }

    @Test
    public void shouldAddParameter() throws Exception {
        Request r = new Request();
        r.add("param", "value");
        assertThat(r.queryString(), equalTo("param=value"));
    }

    @Test
    public void shouldAddOnlyParameterNameIfPassedNullValue() throws Exception {
        Request r = new Request();
        r.add("param", null);
        assertThat(r.queryString(), equalTo("param"));
    }

    @Test
    public void shouldAddAllContainedValuesIfPassedArrays() throws Exception {
        Request r = new Request();
        r.add("foo", new String[] { "1", "2"});
        assertThat(r.queryString(), equalTo("foo=1&foo=2"));
    }

    @Test
    public void shouldAddAllContainedValuesIfPassedIterable() throws Exception {
        Request r = new Request();
        r.add("foo", Arrays.asList("1", "2"));
        assertThat(r.queryString(), equalTo("foo=1&foo=2"));
    }

    @Test
    public void shouldCopyRequestWithNewResource() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        Request p2 = p.newResource("baz");
        assertThat(p, not(sameInstance(p2)));
        assertThat(p2.toString(),
            equalTo("Request{mResource='baz', params=[foo=100, baz=22.3], files=null, entity=null, mToken=null, listener=null}"));
    }

    @Test
    public void shouldImplementIterable() throws Exception {
        Request p = new Request().with("foo", 100, "baz", 22.3f);
        Iterator<NameValuePair> it = p.iterator();
        assertThat(it.next().getName(), equalTo("foo"));
        assertThat(it.next().getName(), equalTo("baz"));
        try {
            it.next();
            throw new RuntimeException("NoSuchElementException expected");
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void shouldGetStringFromHttpResponse() throws Exception {
        HttpResponse resp = mock(HttpResponse.class);
        HttpEntity ent = mock(HttpEntity.class);
        when(ent.getContent()).thenReturn(new ByteArrayInputStream("foo".getBytes()));
        when(resp.getEntity()).thenReturn(ent);

        assertThat(Http.getString(resp), equalTo("foo"));
    }

    @Test
    public void shouldBuildARequest() throws Exception {
        HttpGet request = Request.to("/foo").with("1", "2").buildRequest(HttpGet.class);
        assertThat(request.getURI().toString(), equalTo("/foo?1=2"));
    }

    @Test
    public void shouldAddTokenToHeaderIfSpecified() throws Exception {
        HttpGet request = Request.to("/foo")
                .with("1", "2")
                .usingToken(new Token("acc3ss", "r3fr3sh"))
                .buildRequest(HttpGet.class);

        Header auth = request.getFirstHeader(AUTH.WWW_AUTH_RESP);
        assertNotNull(auth);
        assertThat(auth.getValue(), CoreMatchers.containsString("acc3ss"));
    }

    @Test
    public void shouldAddRangeHeaderIfSpecified() throws Exception {
        HttpGet request = Request.to("/foo")
                .range(1,200)
                .buildRequest(HttpGet.class);

        Header auth = request.getFirstHeader("Range");
        assertNotNull(auth);
        assertThat(auth.getValue(), equalTo("bytes=1-200"));
    }

    @Test
    public void shouldCreateMultipartRequestWhenFilesAreAdded() throws Exception {
        File f = File.createTempFile("testing", "test");

        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("foo", f)
                .buildRequest(HttpPost.class);

        assertTrue(request.getEntity() instanceof MultipartEntity);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String encoded = os.toString();
        assertThat(encoded, containsString("foo"));
        assertThat(encoded, containsString("key"));
        assertThat(encoded, containsString("value"));
        assertThat(encoded, containsString("filename=\"testing"));
    }

    @Test
    public void shouldOverrideFilenameInUpload() throws Exception {
        File f = File.createTempFile("testing", "test");

        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("foo", f, "music.mp3")
                .buildRequest(HttpPost.class);

        assertTrue(request.getEntity() instanceof MultipartEntity);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String encoded = os.toString();

        assertThat(encoded, containsString("foo"));
        assertThat(encoded, containsString("key"));
        assertThat(encoded, containsString("value"));
        assertThat(encoded, containsString("filename=\"music.mp3\""));
    }

    @Test
    public void shouldDetectMultipartRequests() throws Exception {
        assertFalse(Request.to("/foo")
                .with("key", "value").isMultipart());

        assertTrue(Request.to("/foo")
                .with("key", "value")
                .withFile("foo", "foo".getBytes()).isMultipart());
    }

    @Test
    public void shouldUploadByteDataWithFilename() throws Exception {
        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("testing", "foo".getBytes(), "music.mp3")
                .buildRequest(HttpPost.class);

        assertTrue(request.getEntity() instanceof MultipartEntity);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String encoded = os.toString();
        assertThat(encoded, containsString("filename=\"music.mp3\""));
    }

    @Test
    public void shouldPreservePostUri() throws Exception {
        HttpPost request = Request.to("/foo")
                .buildRequest(HttpPost.class);

        assertThat(request.getURI(), notNullValue());
        assertThat(request.getURI().toString(), equalTo("/foo"));
    }

    @Test
    public void shouldCreateMultipartRequestWhenFilesAreAddedWithByteArray() throws Exception {
        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("testing", "foo".getBytes())
                .buildRequest(HttpPost.class);

        assertTrue(request.getEntity() instanceof MultipartEntity);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String encoded = os.toString();

        assertThat(encoded, containsString("foo"));
        assertThat(encoded, containsString("key"));
        assertThat(encoded, containsString("value"));
        assertThat(encoded, containsString("testing"));
    }

    @Test
    public void shouldIncludeAnyEntityInRequest() throws Exception {
        HttpPost request = Request.to("/too")
                .withEntity(new StringEntity("foo"))
                .buildRequest(HttpPost.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String body = os.toString();
        assertThat("foo", equalTo(body));
    }

    @Test
    public void shouldIncludeContentInRequest() throws Exception {
        HttpPost request = Request.to("/too")
                .withContent("<foo><baz>content</baz></foo>", "application/xml")
                .buildRequest(HttpPost.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);
        String body = os.toString();

        assertThat(request.getFirstHeader("Content-Type").getValue(), equalTo("application/xml"));
        assertThat("<foo><baz>content</baz></foo>", equalTo(body));
    }

    @Test
    public void shouldUseUTF8AsDefaultEncodingForStringPayloads() throws Exception {
        HttpPost request = Request.to("/too")
                .withContent("{ string:\"îøüöéí\" }", "application/json")
                .buildRequest(HttpPost.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.getEntity().writeTo(os);

        String decoded = os.toString("UTF-8");
        assertThat("{ string:\"îøüöéí\" }", equalTo(decoded));
    }

    @Test
    public void shouldBuildARequestWithContentAndPreserveQueryParameters() throws Exception {
        HttpPost post = Request
                .to("/foo")
                .withContent("{}", "application/json")
                .with("1", "2").buildRequest(HttpPost.class);

        assertThat(post.getURI().toString(), equalTo("/foo?1=2"));
        assertTrue(post.getEntity() instanceof StringEntity);
        assertThat(post.getEntity().getContentLength(), equalTo(2l));
        assertThat(EntityUtils.toString(post.getEntity()), equalTo("{}"));
        assertThat(post.getFirstHeader("Content-Type").getValue(), equalTo("application/json"));
    }



    @Test
    public void whenAProgressListenerIsSpecifiedShouldHaveCountingMultipart() throws Exception {
        HttpPost request = Request.to("/foo")
                .with("key", "value")
                .withFile("foo", new File("/tmp"))
                .setProgressListener(mock(Request.TransferProgressListener.class))
                .buildRequest(HttpPost.class);
        assertTrue(request.getEntity() instanceof CountingMultipartEntity);
    }

    @Test
    public void shouldDoStringFormattingInFactoryMethod() throws Exception {
        assertThat(Request.to("/resource/%d", 200).toUrl(), equalTo("/resource/200"));
    }

    @Test(expected = IllegalFormatException.class)
    public void shouldThrowIllegalFormatExceptionWhenInvalidParameters() throws Exception {
        Request.to("/resource/%d", "int").toUrl();
    }

    @Test
    public void toStringShouldWork() throws Exception {
        assertThat(
                new Request("/foo").with("1", "2").toString(),
                equalTo("Request{mResource='/foo', params=[1=2], files=null, entity=null, mToken=null, listener=null}"));
    }

    @Test
    public void itShouldParseExistingQueryParameters() throws Exception {
        assertThat(
            new Request("/foo?bar=baz").with("1", "2").toUrl(),
            equalTo("/foo?bar=baz&1=2"));

        assertThat(
            new Request("/foo?").with("1", "2").toUrl(),
            equalTo("/foo?1=2"));

        assertThat(
            new Request("/foo?bar=baz&foo=bar").with("1", "2").toUrl(),
            equalTo("/foo?bar=baz&foo=bar&1=2"));

        String s3 = "http://ak-media.soundcloud.com/XAGeEabPextR.128.mp3?AWSAccessKeyId=AKIAJBHW5FB4ERKUQUOQ&Expires=1319547723&Signature=o53ozj2b%2BrdARFBEZoAziK7mWIY%3D&__gda__=1319547723_e7e8d73cf3af2b003d891ecc01c20143";

        assertThat(Request.to(s3).toUrl(), equalTo(s3));

    }

    @Test
    public void itShouldParseFullURI() throws Exception {
        assertThat(
             new Request(URI.create("http://foo.soundcloud.com/foo?bar=baz")).with("1", "2").toUrl(),
             equalTo("/foo?bar=baz&1=2"));

        assertThat(
             new Request(URI.create("http://foo.soundcloud.com/foo")).with("1", "2").toUrl(),
             equalTo("/foo?1=2"));

        assertThat(
             new Request(URI.create("http://foo.soundcloud.com/")).toUrl(),
             equalTo("/"));
    }

    @Test
    public void shouldHaveCopyConstructor() {
        Request orig = new Request("/foo").with("1", 2, "3",4);
        Request copy = new Request(orig);
        assertThat(copy.toUrl(),equalTo(orig.toUrl()));
        assertThat(copy.getToken(),equalTo(orig.getToken()));
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptNullStringInCtor() throws Exception {
        new Request((String) null);
    }

    @Test
    public void shouldNotModifyOriginal() {
        Request orig = new Request("/foo").with("1", 2, "3",4);
        orig.setProgressListener(new Request.TransferProgressListener(){
            @Override public void transferred(long amount) {}
        });
        Request copy = new Request(orig);
        orig.add("cursor","asdf");
        orig.usingToken(new Token("access","refresh"));
        assertThat(copy.toUrl(), not(equalTo(orig.toUrl())));
        assertThat(copy.getToken(), not(equalTo(orig.getToken())));
        assertThat(orig.getListener(),equalTo(copy.getListener()));
    }

    @Test
    public void testFormatRange() throws Exception {
        assertThat(Request.formatRange(1, 1000), equalTo("bytes=1-1000"));
        assertThat(Request.formatRange(1), equalTo("bytes=1-"));
        assertThat(Request.formatRange(), equalTo("bytes=0-"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatRangeInvalidArgument() throws Exception {
        Request.formatRange(100,200,300);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatRangeInvalidArgument2() throws Exception {
        Request.formatRange(1000, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatRangeInvalidArgument3() throws Exception {
        Request.formatRange(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatRangeInvalidArgument4() throws Exception {
        Request.formatRange(-1, 200);
    }
}
