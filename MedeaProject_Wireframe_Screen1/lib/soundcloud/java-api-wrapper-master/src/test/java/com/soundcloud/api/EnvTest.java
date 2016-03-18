package com.soundcloud.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpHost;
import org.junit.Test;


public class EnvTest {
    @Test
    public void testIsApiHost() throws Exception {
        assertTrue(Env.LIVE.isApiHost(new HttpHost("api.soundcloud.com", 80, "http")));
        assertTrue(Env.LIVE.isApiHost(new HttpHost("api.soundcloud.com", 443, "https")));
        assertFalse(Env.LIVE.isApiHost(new HttpHost("foo.soundcloud.com", 443, "https")));
    }

    @Test
    public void shouldHostsShouldExplicitlySpecifyPorts() throws Exception {
        assertEquals(80, Env.LIVE.authResourceHost.getPort());
        assertEquals(443, Env.LIVE.sslAuthResourceHost.getPort());

        assertEquals(80, Env.LIVE.resourceHost.getPort());
        assertEquals(443, Env.LIVE.sslResourceHost.getPort());
    }
}
