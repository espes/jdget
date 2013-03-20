package org.appwork.utils.net.httpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.appwork.utils.Regex;
import org.appwork.utils.encoding.Base64;

public class HTTPProxyHTTPConnectionImpl extends HTTPConnectionImpl {
    private int               httpPort;
    private String            httpHost;
    private StringBuilder     proxyRequest;
    private InetSocketAddress proxyInetSocketAddress = null;

    private boolean           preferConnectMethod    = true;
    private SSLException      sslException           = null;

    public HTTPProxyHTTPConnectionImpl(final URL url, final HTTPProxy p) {
        super(url, p);
        preferConnectMethod = p.isConnectMethodPrefered();
        setRequestProperty("Proxy-Connection", "close");
    }

    /*
     * SSL over HTTP Proxy, see
     * http://muffin.doit.org/docs/rfc/tunneling_ssl.html
     */
    @Override
    public void connect() throws IOException {
        if (isConnected()) { return;/* oder fehler */
        }
        try {
            if (proxy == null || !proxy.getType().equals(HTTPProxy.TYPE.HTTP)) { throw new IOException("HTTPProxyHTTPConnection: invalid HTTP Proxy!"); }
            if (proxy.getPass() != null && proxy.getPass().length() > 0 || proxy.getUser() != null && proxy.getUser().length() > 0) {
                /* add proxy auth in case username/pw are set */
                final String user = proxy.getUser() == null ? "" : proxy.getUser();
                final String pass = proxy.getPass() == null ? "" : proxy.getPass();
                requestProperties.put("Proxy-Authorization", "Basic " + new String(Base64.encodeToByte((user + ":" + pass).getBytes(), false)));
            }
            final InetAddress hosts[] = resolvHostIP(proxy.getHost());
            IOException ee = null;
            long startTime = System.currentTimeMillis();
            for (final InetAddress host : hosts) {
                httpSocket = new Socket();
                httpSocket.setSoTimeout(readTimeout);
                try {
                    /* create and connect to socks5 proxy */
                    startTime = System.currentTimeMillis();
                    httpSocket.connect(proxyInetSocketAddress = new InetSocketAddress(host, proxy.getPort()), connectTimeout);
                    /* connection is okay */
                    ee = null;
                    break;
                } catch (final IOException e) {
                    /* connection failed, try next available ip */
                    proxyInetSocketAddress = null;
                    try {
                        httpSocket.close();
                    } catch (final Throwable e2) {
                    }
                    ee = e;
                }
            }
            if (ee != null) { throw new ProxyConnectException(ee, proxy); }
            requestTime = System.currentTimeMillis() - startTime;
            if (httpURL.getProtocol().startsWith("https") || isConnectMethodPrefered()) {
                /* ssl via CONNECT method or because we prefer CONNECT */
                /* build CONNECT request */
                proxyRequest = new StringBuilder();
                proxyRequest.append("CONNECT ");
                proxyRequest.append(httpURL.getHost() + ":" + (httpURL.getPort() != -1 ? httpURL.getPort() : httpURL.getDefaultPort()));
                proxyRequest.append(" HTTP/1.1\r\n");
                if (requestProperties.get("User-Agent") != null) {
                    proxyRequest.append("User-Agent: " + requestProperties.get("User-Agent") + "\r\n");
                }
                if (requestProperties.get("Host") != null) {
                    /* use existing host header */
                    proxyRequest.append("Host: " + requestProperties.get("Host") + "\r\n");
                } else {
                    /* add host from url as fallback */
                    proxyRequest.append("Host: " + httpURL.getHost() + "\r\n");
                }
                if (requestProperties.get("Proxy-Authorization") != null) {
                    proxyRequest.append("Proxy-Authorization: " + requestProperties.get("Proxy-Authorization") + "\r\n");
                }
                proxyRequest.append("\r\n");
                /* send CONNECT to proxy */
                httpSocket.getOutputStream().write(proxyRequest.toString().getBytes("UTF-8"));
                httpSocket.getOutputStream().flush();
                /* parse CONNECT response */
                ByteBuffer header = HTTPConnectionUtils.readheader(httpSocket.getInputStream(), true);
                byte[] bytes = new byte[header.limit()];
                header.get(bytes);
                final String proxyResponseStatus = new String(bytes, "ISO-8859-1").trim();
                proxyRequest.append(proxyResponseStatus + "\r\n");
                String proxyCode = null;
                if (proxyResponseStatus.startsWith("HTTP")) {
                    /* parse response code */
                    proxyCode = new Regex(proxyResponseStatus, "HTTP.*? (\\d+)").getMatch(0);
                }
                if (!"200".equals(proxyCode)) {
                    /* something went wrong */
                    try {
                        httpSocket.close();
                    } catch (final Throwable nothing) {
                    }
                    if ("407".equals(proxyCode)) {
                        /* auth invalid/missing */
                        throw new ProxyAuthException(proxy);
                    }

                    throw new ProxyConnectException(proxy);
                }
                /* read rest of CONNECT headers */
                /*
                 * Again, the response follows the HTTP/1.0 protocol, so the
                 * response line starts with the protocol version specifier, and
                 * the response line is followed by zero or more response
                 * headers, followed by an empty line. The line separator is CR
                 * LF pair, or a single LF.
                 */
                while (true) {
                    /*
                     * read line by line until we reach the single empty line as
                     * seperator
                     */
                    header = HTTPConnectionUtils.readheader(httpSocket.getInputStream(), true);
                    if (header.limit() <= 2) {
                        /* empty line, <=2, as it may contains \r and/or \n */
                        break;
                    }
                    bytes = new byte[header.limit()];
                    header.get(bytes);
                    final String temp = new String(bytes, "UTF-8").trim();
                    proxyRequest.append(temp + "\r\n");
                }
                httpPort = httpURL.getPort();
                httpHost = httpURL.getHost();
                if (httpPort == -1) {
                    httpPort = httpURL.getDefaultPort();
                }
                if (httpURL.getProtocol().startsWith("https")) {
                    SSLSocket sslSocket = null;
                    try {
                        final SSLSocketFactory socketFactory = TrustALLSSLFactory.getSSLFactoryTrustALL();
                        sslSocket = (SSLSocket) socketFactory.createSocket(httpSocket, httpHost, httpPort, true);
                        if (sslException != null && sslException.getMessage().contains("bad_record_mac")) {
                            /* workaround for SSLv3 only hosts */
                            sslSocket.setEnabledProtocols(new String[] { "SSLv3" });
                        }
                        sslSocket.startHandshake();
                    } catch (final SSLHandshakeException e) {
                        try {
                            sslSocket.close();
                        } catch (final Throwable e3) {
                        }
                        try {
                            httpSocket.close();
                        } catch (final Throwable e2) {
                        }
                        throw new IOException("HTTPProxyHTTPConnection: " + e, e);
                    }
                    httpSocket = sslSocket;
                }
                /* httpPath needs to be like normal http request, eg /index.html */
                httpPath = new org.appwork.utils.Regex(httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
                if (httpPath == null) {
                    httpPath = "/";
                }
            } else {
                /* direct connect via proxy */
                /*
                 * httpPath needs to include complete path here, eg
                 * http://google.de/
                 */
                proxyRequest = new StringBuilder("DIRECT\r\n");
                httpPath = httpURL.toString();
            }
            /* now send Request */
            sendRequest();
        } catch (final javax.net.ssl.SSLException e) {
            if (sslException != null) {
                throw new ProxyConnectException(e, proxy);
            } else {
                this.disconnect(true);
                sslException = e;
                connect();
            }
        } catch (final IOException e) {
            try {
                this.disconnect();
            } catch (final Throwable e2) {
            }
            if (e instanceof HTTPProxyException) { throw e; }
            throw new ProxyConnectException(e, proxy);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        connectInputStream();
        if (getResponseCode() == 407) {
            /* auth invalid/missing */
            throw new ProxyAuthException(proxy);
        }
        if (getResponseCode() == 504) { throw new ConnectException(getResponseCode() + " " + getResponseMessage()); }
        return super.getInputStream();
    }

    @Override
    protected String getRequestInfo() {
        if (proxyRequest != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("-->HTTPProxy:").append(proxy.getHost() + ":" + proxy.getPort()).append("\r\n");
            if (proxyInetSocketAddress != null && proxyInetSocketAddress.getAddress() != null) {
                sb.append("-->HTTPProxyIP:").append(proxyInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
            }
            sb.append("----------------CONNECTRequest(HTTP)------------\r\n");
            sb.append(proxyRequest.toString());
            sb.append("------------------------------------------------\r\n");
            sb.append(super.getRequestInfo());
            return sb.toString();
        }
        return super.getRequestInfo();
    }

    public boolean isConnectMethodPrefered() {
        return preferConnectMethod;
    }

    public void setPreferConnectMethod(final boolean preferConnectMethod) {
        this.preferConnectMethod = preferConnectMethod;
    }
}
