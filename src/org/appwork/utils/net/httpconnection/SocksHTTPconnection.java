/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpconnection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;

/**
 * @author daniel
 * 
 */
public abstract class SocksHTTPconnection extends HTTPConnectionImpl {

    protected static enum AUTH {
        PLAIN,
        NONE
    }

    protected Socket      sockssocket      = null;
    protected InputStream socksinputstream = null;

    protected InputStream getSocksInputStream() {
        return this.socksinputstream;
    }

    protected OutputStream getSocksOutputStream() {
        return this.socksoutputstream;
    }

    protected OutputStream      socksoutputstream      = null;
    protected int               httpPort;
    protected String            httpHost;
    protected StringBuffer      proxyRequest           = null;
    protected InetSocketAddress proxyInetSocketAddress = null;

    public SocksHTTPconnection(final URL url, final HTTPProxy proxy) {
        super(url, proxy);
    }

    abstract protected void authenticateProxyPlain() throws IOException;

    @Override
    public void connect() throws IOException {
        boolean sslSNIWorkAround = false;
        boolean sslV3Workaround = false;
        InetAddress hosts[] = null;
        connect: while (true) {
            if (this.isConnectionSocketValid()) { return;/* oder fehler */
            }
            this.resetConnection();
            try {
                this.validateProxy();
                IOException ee = null;
                long startTime = System.currentTimeMillis();
                if (hosts == null) {
                    hosts = this.resolvHostIP(this.proxy.getHost());
                }
                for (final InetAddress host : hosts) {
                    this.resetConnection();
                    this.sockssocket = new Socket(Proxy.NO_PROXY);
                    this.sockssocket.setSoTimeout(this.connectTimeout);
                    try {
                        /* create and connect to socks5 proxy */
                        startTime = System.currentTimeMillis();
                        this.sockssocket.connect(this.proxyInetSocketAddress = new InetSocketAddress(host, this.proxy.getPort()), this.connectTimeout);
                        /* connection is okay */
                        ee = null;
                        break;
                    } catch (final IOException e) {
                        try {
                            this.sockssocket.close();
                        } catch (final Throwable e2) {
                        }
                        /* connection failed, try next available ip */
                        this.connectExceptions.add(this.proxyInetSocketAddress + "|" + e.getMessage());
                        ee = e;
                    }
                }
                if (ee != null) { throw new ProxyConnectException(ee, this.proxy); }
                this.socksinputstream = this.sockssocket.getInputStream();
                this.socksoutputstream = this.sockssocket.getOutputStream();
                /* establish connection to socks */
                this.proxyRequest = new StringBuffer();
                final AUTH auth = this.sayHello();
                switch (auth) {
                case PLAIN:
                    this.proxyRequest.append("<-PLAIN AUTH\r\n");
                    /* username/password authentication */
                    this.authenticateProxyPlain();
                    break;
                case NONE:
                    this.proxyRequest.append("<-NONE AUTH\r\n");
                    break;
                }
                /* establish to destination through socks */
                this.httpPort = this.httpURL.getPort();
                this.httpHost = this.httpURL.getHost();
                if (this.httpPort == -1) {
                    this.httpPort = this.httpURL.getDefaultPort();
                }
                final Socket establishedConnection = this.establishConnection();
                if (this.httpURL.getProtocol().startsWith("https")) {
                    /* we need to lay ssl over normal socks5 connection */
                    SSLSocket sslSocket = null;
                    try {
                        if (sslSNIWorkAround) {
                            /* wrong configured SNI at serverSide */
                            sslSocket = (SSLSocket) TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket(this.sockssocket, "", this.httpPort, true);
                        } else {
                            sslSocket = (SSLSocket) TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket(this.sockssocket, this.httpURL.getHost(), this.httpPort, true);
                        }
                        if (sslV3Workaround && sslSocket != null) {
                            /* workaround for SSLv3 only hosts */
                            sslSocket.setEnabledProtocols(new String[] { "SSLv3" });
                        }
                        sslSocket.startHandshake();
                    } catch (final SSLHandshakeException e) {
                        this.disconnect();
                        if (sslSNIWorkAround == false && e.getMessage().contains("unrecognized_name")) {
                            sslSNIWorkAround = true;
                            continue connect;
                        } else if (sslV3Workaround == false && e.getMessage().contains("bad_record_mac")) {
                            sslV3Workaround = true;
                            continue connect;
                        }
                        throw new ProxyConnectException(e, this.proxy);
                    }
                    this.connectionSocket = sslSocket;
                } else {
                    /* we can continue to use the socks connection */
                    this.connectionSocket = establishedConnection;
                }
                this.setReadTimeout(this.readTimeout);
                this.httpResponseCode = -1;
                this.requestTime = System.currentTimeMillis() - startTime;
                this.httpPath = new org.appwork.utils.Regex(this.httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
                if (this.httpPath == null) {
                    this.httpPath = "/";
                }
                /* now send Request */
                this.sendRequest();
                return;
            } catch (final javax.net.ssl.SSLException e) {
                this.connectExceptions.add(this.proxyInetSocketAddress + "|" + e.getMessage());
                this.disconnect();
                if (sslSNIWorkAround == false && e.getMessage().contains("unrecognized_name")) {
                    sslSNIWorkAround = true;
                    continue connect;
                } else if (sslV3Workaround == false && e.getMessage().contains("bad_record_mac")) {
                    sslV3Workaround = true;
                    continue connect;
                }
                throw e;
            } catch (final IOException e) {
                this.disconnect();
                if (e instanceof HTTPProxyException) { throw e; }
                this.connectExceptions.add(this.proxyInetSocketAddress + "|" + e.getMessage());
                throw new ProxyConnectException(e, this.proxy);
            }
        }
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        try {
            this.readTimeout = Math.max(0, readTimeout);
            this.sockssocket.setSoTimeout(this.readTimeout);
            this.connectionSocket.setSoTimeout(this.readTimeout);
        } catch (final Throwable ignore) {
        }
    }

    @Override
    protected boolean isKeepAlivedEnabled() {
        return false;
    }

    @Override
    public void disconnect() {
        try {
            super.disconnect();
        } finally {
            try {
                if (this.sockssocket != null) {
                    this.sockssocket.close();
                }
            } catch (final Throwable e) {
                this.sockssocket = null;
            }
        }
    }

    abstract protected Socket establishConnection() throws IOException;

    @Override
    protected String getRequestInfo() {
        if (this.proxyRequest != null) {
            final StringBuilder sb = new StringBuilder();
            final String type = this.proxy.getType().name();
            sb.append("-->" + type + ":").append(this.proxy.getHost() + ":" + this.proxy.getPort()).append("\r\n");
            if (this.proxyInetSocketAddress != null && this.proxyInetSocketAddress.getAddress() != null) {
                sb.append("-->" + type + "IP:").append(this.proxyInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
            }
            sb.append("----------------CONNECTRequest(" + type + ")----------\r\n");
            sb.append(this.proxyRequest.toString());
            sb.append("------------------------------------------------\r\n");
            sb.append(super.getRequestInfo());
            return sb.toString();
        }
        return super.getRequestInfo();
    }

    /* reads response with expLength bytes */
    protected byte[] readResponse(final int expLength) throws IOException {
        final byte[] response = new byte[expLength];
        int index = 0;
        int read = 0;
        final InputStream inputStream = this.getSocksInputStream();
        while (index < expLength && (read = inputStream.read()) != -1) {
            response[index] = (byte) read;
            index++;
        }
        if (index < expLength) { throw new EOFException("SocksHTTPConnection: not enough data read"); }
        return response;
    }

    abstract protected AUTH sayHello() throws IOException;

    abstract protected void validateProxy() throws IOException;
}
