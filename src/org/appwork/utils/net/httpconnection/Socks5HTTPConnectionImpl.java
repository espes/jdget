package org.appwork.utils.net.httpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Socks5HTTPConnectionImpl extends HTTPConnectionImpl {

    protected Socket       socks5socket       = null;
    protected InputStream  socks5inputstream  = null;
    protected OutputStream socks5outputstream = null;
    private int            httpPort;
    private String         httpHost;
    private StringBuilder  proxyRequest;

    public Socks5HTTPConnectionImpl(final URL url, final HTTPProxy proxy) {
        super(url, proxy);
    }

    protected void authenticateProxy() throws IOException {
        try {
            final String user = this.proxy.getUser() == null ? "" : this.proxy.getUser();
            final String pass = this.proxy.getPass() == null ? "" : this.proxy.getPass();
            this.proxyRequest = new StringBuilder("AUTH user:pass\r\n");
            final byte[] username = user.getBytes("UTF-8");
            final byte[] password = pass.getBytes("UTF-8");
            /* must be 1 */
            this.socks5outputstream.write((byte) 1);
            /* send username */
            this.socks5outputstream.write((byte) username.length);
            this.socks5outputstream.write(username);
            /* send password */
            this.socks5outputstream.write((byte) password.length);
            this.socks5outputstream.write(password);
            /* read response, 2 bytes */
            final byte[] resp = this.readResponse(2);
            if (resp[0] != 1) { throw new IOException("Socks5HTTPConnection: invalid Socks5 response"); }
            if (resp[1] != 0) {
                this.proxy.setStatus(HTTPProxy.STATUS.INVALIDAUTH);
                throw new ProxyAuthException();
            }
        } catch (final IOException e) {
            try {
                this.socks5socket.close();
            } catch (final Throwable e2) {
            }
            throw e;
        }
    }

    @Override
    public void connect() throws IOException {
        if (this.isConnected()) { return;/* oder fehler */
        }
        if (this.proxy == null || !this.proxy.getType().equals(HTTPProxy.TYPE.SOCKS5)) { throw new IOException("Socks5HTTPConnection: invalid Socks5 Proxy!"); }
        /* create and connect to socks5 proxy */
        this.socks5socket = new Socket();
        this.socks5socket.setSoTimeout(this.readTimeout);
        final long startTime = System.currentTimeMillis();
        try {
            this.socks5socket.connect(new InetSocketAddress(this.proxy.getHost(), this.proxy.getPort()), this.connectTimeout);
        } catch (final IOException e) {
            this.proxy.setStatus(HTTPProxy.STATUS.OFFLINE);
            throw new ProxyConnectException(e.getMessage());
        }
        this.socks5inputstream = this.socks5socket.getInputStream();
        this.socks5outputstream = this.socks5socket.getOutputStream();
        /* establish connection to socks5 */
        final int method = this.sayHello();
        if (method == 2) {
            /* username/password authentication */
            this.authenticateProxy();
        } else {
            this.proxyRequest = new StringBuilder("NONE AUTH\r\n");
        }
        /* establish to destination through socks5 */
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
                final SSLSocketFactory socketFactory = TrustALLSSLFactory.getSSLFactoryTrustALL();
                sslSocket = (SSLSocket) socketFactory.createSocket(establishedConnection, this.httpHost, this.httpPort, true);
                sslSocket.startHandshake();
            } catch (final SSLHandshakeException e) {
                try {
                    this.socks5socket.close();
                } catch (final Throwable e2) {
                }
                throw new IOException("Socks5HTTPConnection: " + e);
            }
            this.httpSocket = sslSocket;
        } else {
            /* we can continue to use the socks5 connection */
            this.httpSocket = establishedConnection;
        }
        /* update connection stats of proxy */
        if (this.proxy != null) {
            this.proxy.getUsedConnections().incrementAndGet();
            this.proxy.getCurrentConnections().incrementAndGet();
        }
        this.httpResponseCode = -1;
        this.requestTime = System.currentTimeMillis() - startTime;
        this.httpPath = new org.appwork.utils.Regex(this.httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
        if (this.httpPath == null) {
            this.httpPath = "/";
        }
        /* now send Request */
        this.sendRequest();
    }

    @Override
    public void disconnect() {
        if (this.isConnected()) {
            try {
                if (!this.connectionClosed) {
                    this.connectionClosed = true;
                    /* update connection stats of proxy */
                    if (this.proxy != null) {
                        this.proxy.getCurrentConnections().decrementAndGet();
                    }
                }
                this.httpSocket.close();
            } catch (final Throwable e) {
            }
        }
        try {
            this.socks5socket.close();
        } catch (final Throwable e) {
        }
    }

    protected Socket establishConnection() throws IOException {
        try {
            /* socks5 */
            this.socks5outputstream.write((byte) 5);
            /* tcp/ip connection */
            this.socks5outputstream.write((byte) 1);
            /* reserved */
            this.socks5outputstream.write((byte) 0);
            /* we use domain names */
            this.socks5outputstream.write((byte) 3);
            /* send somain name */
            final byte[] domain = this.httpHost.getBytes("UTF-8");
            this.socks5outputstream.write((byte) domain.length);
            this.socks5outputstream.write(domain);
            /* send port */
            /* network byte order */
            this.socks5outputstream.write(this.httpPort >> 8 & 0xff);
            this.socks5outputstream.write(this.httpPort & 0xff);
            this.socks5outputstream.flush();
            /* read response, 4 bytes and then read rest of response */
            final byte[] resp = this.readResponse(4);
            if (resp[0] != 5) { throw new IOException("Socks5HTTPConnection: invalid Socks5 response"); }
            switch (resp[1]) {
            case 0:
                break;
            case 3:
                throw new SocketException("Network is unreachable");
            case 4:
                throw new SocketException("Host is unreachable");
            case 5:
                throw new ConnectException("Connection refused");
            case 1:
            case 2:
            case 6:
            case 7:
            case 8:
                throw new ConnectException("Socks5HTTPConnection: could not establish connection, status=" + resp[1]);
            }
            if (resp[3] == 1) {
                /* ip4v response */
                this.readResponse(4 + 2);
            } else if (resp[3] == 3) {
                /* domain name response */
                this.readResponse(1 + domain.length + 2);
            } else {
                throw new IOException("Socks5HTTPConnection: unsupported address Type " + resp[3]);
            }
            return this.socks5socket;
        } catch (final IOException e) {
            try {
                this.socks5socket.close();
            } catch (final Throwable e2) {
            }
            throw e;
        }
    }

    @Override
    protected String getRequestInfo() {
        if (this.proxyRequest != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("-->Socks5Proxy:").append(this.proxy.getHost() + ":" + this.proxy.getPort()).append("\r\n");
            sb.append("----------------CONNECTRequest------------------\r\n");
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
        while (index < expLength && (read = this.socks5inputstream.read()) != -1) {
            response[index] = (byte) read;
            index++;
        }
        if (index < expLength) { throw new IOException("Socks5HTTPConnection: not enough data read"); }
        return response;
    }

    protected int sayHello() throws IOException {
        try {
            /* socks5 */
            this.socks5outputstream.write((byte) 5);
            /* only none ans password/username auth method */
            this.socks5outputstream.write((byte) 2);
            /* none */
            this.socks5outputstream.write((byte) 2);
            /* username/password */
            this.socks5outputstream.write((byte) 0);
            this.socks5outputstream.flush();
            /* read response, 2 bytes */
            final byte[] resp = this.readResponse(2);
            if (resp[0] != 5) { throw new IOException("Socks5HTTPConnection: invalid Socks5 response"); }
            if (resp[1] == 255) { throw new IOException("Socks5HTTPConnection: no acceptable authentication method found"); }
            return resp[1];
        } catch (final IOException e) {
            try {
                this.socks5socket.close();
            } catch (final Throwable e2) {
            }
            throw e;
        }
    }
}
