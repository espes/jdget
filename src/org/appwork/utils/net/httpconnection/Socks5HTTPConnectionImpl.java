package org.appwork.utils.net.httpconnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import org.appwork.utils.StringUtils;

public class Socks5HTTPConnectionImpl extends SocksHTTPconnection {

    public Socks5HTTPConnectionImpl(final URL url, final HTTPProxy proxy) {
        super(url, proxy);
    }

    @Override
    protected void authenticateProxyPlain() throws IOException {
        try {
            final String user = this.proxy.getUser() == null ? "" : this.proxy.getUser();
            final String pass = this.proxy.getPass() == null ? "" : this.proxy.getPass();
            this.proxyRequest.append("->AUTH user:pass\r\n");
            final byte[] username = user.getBytes("ISO-8859-1");
            final byte[] password = pass.getBytes("ISO-8859-1");
            final OutputStream outputStream = this.getSocksOutputStream();
            /* must be 1 */
            outputStream.write((byte) 1);
            /* send username */
            outputStream.write((byte) username.length);
            outputStream.write(username);
            /* send password */
            outputStream.write((byte) password.length);
            outputStream.write(password);
            /* read response, 2 bytes */
            final byte[] resp = this.readResponse(2);
            if (resp[0] != 1) { throw new ProxyConnectException(this.proxy); }
            if (resp[1] != 0) {
                this.proxyRequest.append("<-AUTH Invalid!\r\n");
                throw new ProxyAuthException(this.proxy);
            } else {
                this.proxyRequest.append("<-AUTH Valid!\r\n");
            }
        } catch (final IOException e) {
            try {
                this.sockssocket.close();
            } catch (final Throwable e2) {
            }
            if (e instanceof HTTPProxyException) { throw e; }
            throw new ProxyConnectException(e, this.proxy);
        }
    }

    @Override
    protected Socket establishConnection() throws IOException {
        try {
            final OutputStream outputStream = this.getSocksOutputStream();
            /* socks5 */
            outputStream.write((byte) 5);
            /* tcp/ip connection */
            outputStream.write((byte) 1);
            /* reserved */
            outputStream.write((byte) 0);
            /* we use domain names */
            outputStream.write((byte) 3);
            /* send domain name */
            this.proxyRequest.append("->SEND tcp connect request by domain\r\n");
            final byte[] domain = this.httpHost.getBytes("ISO-8859-1");
            outputStream.write((byte) domain.length);
            outputStream.write(domain);
            /* send port */
            /* network byte order */
            outputStream.write(this.httpPort >> 8 & 0xff);
            outputStream.write(this.httpPort & 0xff);
            outputStream.flush();
            /* read response, 4 bytes and then read rest of response */
            final byte[] resp = this.readResponse(4);
            if (resp[0] != 5) { throw new ProxyConnectException("Socks5HTTPConnection: invalid Socks5 response", this.proxy); }
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
                throw new ProxyConnectException("Socks5HTTPConnection: connection not allowed by ruleset", this.proxy);
            case 6:
            case 7:
            case 8:
                throw new ProxyConnectException("Socks5HTTPConnection: could not establish connection, status=" + resp[1], this.proxy);
            }
            if (resp[3] == 1) {
                /* ip4v response */
                this.readResponse(4 + 2);
                this.proxyRequest.append("<-CONNECT IP\r\n");
            } else if (resp[3] == 3) {
                /* domain name response */
                this.readResponse(1 + domain.length + 2);
                this.proxyRequest.append("<-CONNECT Domain\r\n");
            } else {
                throw new ProxyConnectException("Socks5HTTPConnection: unsupported address Type " + resp[3], this.proxy);
            }
            return this.sockssocket;
        } catch (final IOException e) {
            this.disconnect();
            if (e instanceof HTTPProxyException) { throw e; }
            throw new ProxyConnectException(e, this.proxy);
        }
    }

    @Override
    protected AUTH sayHello() throws IOException {
        try {
            this.proxyRequest.append("->SOCKS5 Hello\r\n");
            /* socks5 */
            final OutputStream outputStream = this.getSocksOutputStream();
            outputStream.write((byte) 5);
            /* only none ans password/username auth method */
            boolean plainAuthPossible = false;
            if (!StringUtils.isEmpty(this.proxy.getUser()) || !StringUtils.isEmpty(this.proxy.getPass())) {
                plainAuthPossible = true;
            }
            if (plainAuthPossible) {
                outputStream.write((byte) 2);
                this.proxyRequest.append("->SOCKS5 Offer None&Plain Authentication\r\n");
                /* none */
                outputStream.write((byte) 0);
                /* username/password */
                outputStream.write((byte) 2);
                outputStream.flush();
            } else {
                outputStream.write((byte) 1);
                this.proxyRequest.append("->SOCKS5 Offer None Authentication\r\n");
                /* none */
                outputStream.write((byte) 0);
                outputStream.flush();
            }
            /* read response, 2 bytes */
            final byte[] resp = this.readResponse(2);
            if (resp[0] != 5) { throw new ProxyConnectException("Socks5HTTPConnection: invalid Socks5 response", this.proxy); }
            if (resp[1] == 255) {
                this.proxyRequest.append("<-SOCKS5 Authentication Denied\r\n");
                throw new ProxyConnectException("Socks5HTTPConnection: no acceptable authentication method found", this.proxy);
            }
            if (resp[1] == 2) {
                if (plainAuthPossible == false) {
                    this.proxyRequest.append("->SOCKS5 Plain auth required but not offered!\r\n");
                }
                return AUTH.PLAIN;
            }
            if (resp[1] == 0) { return AUTH.NONE; }
            throw new IOException("Unsupported auth " + resp[1]);
        } catch (final IOException e) {
            this.disconnect();
            if (e instanceof HTTPProxyException) { throw e; }
            throw new ProxyConnectException(e, this.proxy);
        }
    }

    @Override
    protected void validateProxy() throws IOException {
        if (this.proxy == null || !HTTPProxy.TYPE.SOCKS5.equals(this.proxy.getType())) { throw new ProxyConnectException("Socks5HTTPConnection: invalid Socks5 Proxy!", this.proxy); }
    }
}
