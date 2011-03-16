package org.appwork.utils.net.httpconnection;

import java.net.URL;

public class HTTPConnectionFactory {

    public static HTTPConnection createHTTPConnection(final URL url, final HTTPProxy proxy) {
        if (proxy == null) { return new HTTPConnectionImpl(url); }
        if (proxy.getType().equals(HTTPProxy.TYPE.NONE)) { return new HTTPConnectionImpl(url); }
        if (proxy.getType().equals(HTTPProxy.TYPE.DIRECT)) { return new HTTPConnectionImpl(url, proxy); }
        if (proxy.getType().equals(HTTPProxy.TYPE.SOCKS5)) { return new Socks5HTTPConnectionImpl(url, proxy); }
        if (proxy.getType().equals(HTTPProxy.TYPE.HTTP)) { return new HTTPProxyHTTPConnectionImpl(url, proxy); }
        throw new RuntimeException("unsupported proxy type: " + proxy.getType().name());
    }
}
