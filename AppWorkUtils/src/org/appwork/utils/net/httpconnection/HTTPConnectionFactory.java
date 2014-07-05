package org.appwork.utils.net.httpconnection;

import java.net.URL;

public class HTTPConnectionFactory {

    public static HTTPConnection createHTTPConnection(final URL url, final HTTPProxy proxy) {
        if (proxy == null) { return new HTTPConnectionImpl(url); }
        if (proxy.isPreferNativeImplementation()) { return new NativeHTTPConnectionImpl(url, proxy); }
        if (proxy.isNone() || proxy.isDirect()) { return new HTTPConnectionImpl(url, proxy); }
        if (proxy.getType().equals(HTTPProxy.TYPE.SOCKS5)) { return new Socks5HTTPConnectionImpl(url, proxy); }
        if (proxy.getType().equals(HTTPProxy.TYPE.SOCKS4)) { return new Socks4HTTPConnectionImpl(url, proxy); }
        if (proxy.getType().equals(HTTPProxy.TYPE.HTTP)) { return new HTTPProxyHTTPConnectionImpl(url, proxy); }
        throw new RuntimeException("unsupported proxy type: " + proxy.getType().name());
    }
}
