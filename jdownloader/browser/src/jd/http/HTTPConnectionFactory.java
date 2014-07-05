package jd.http;

import java.net.URL;

import org.appwork.utils.net.httpconnection.HTTPProxy;

public class HTTPConnectionFactory {

    public static URLConnectionAdapter createHTTPConnection(final URL url, final HTTPProxy proxy) {
        if (proxy == null) {
            return new URLConnectionAdapterDirectImpl(url);
        } else if (proxy.isPreferNativeImplementation()) {
            return new URLConnectionAdapterNative(url, proxy);
        } else if (proxy.isNone() || proxy.isDirect()) {
            return new URLConnectionAdapterDirectImpl(url, proxy);
        } else if (proxy.getType().equals(HTTPProxy.TYPE.SOCKS5)) {
            return new URLConnectionAdapterSocks5Impl(url, proxy);
        } else if (proxy.getType().equals(HTTPProxy.TYPE.SOCKS4)) {
            return new URLConnectionAdapterSocks4Impl(url, proxy);
        } else if (proxy.getType().equals(HTTPProxy.TYPE.HTTP)) {
            return new URLConnectionAdapterHTTPProxyImpl(url, proxy);
        } else {
            throw new RuntimeException("unsupported proxy type: " + proxy.getType().name());
        }
    }
}
