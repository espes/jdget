package org.appwork.utils.net.httpconnection;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.utils.Regex;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;

public class HTTPProxy {

    public static enum STATUS {
        OK,
        OFFLINE,
        INVALIDAUTH
    }

    public static enum TYPE {
        NONE,
        DIRECT,
        SOCKS5,
        HTTP
    }

    private final AtomicLong      usedConnections    = new AtomicLong(0);
    private final AtomicInteger   currentConnections = new AtomicInteger(0);

    public static final HTTPProxy NONE               = new HTTPProxy(TYPE.NONE) {

                                                         @Override
                                                         public void setLocalIP(final InetAddress localIP) {
                                                         }

                                                         @Override
                                                         public void setPass(final String pass) {
                                                         }

                                                         @Override
                                                         public void setPort(final int port) {
                                                         }

                                                         @Override
                                                         public void setStatus(final STATUS status) {
                                                         }

                                                         @Override
                                                         public void setType(final TYPE type) {
                                                         }

                                                         @Override
                                                         public void setUser(final String user) {
                                                         }

                                                     };

    public static HTTPProxy getHTTPProxy(final HTTPProxyStorable storable) {
        if (storable == null || storable.getType() == null) { return null; }
        HTTPProxy ret = null;
        switch (storable.getType()) {
        case NONE:
            return HTTPProxy.NONE;
        case DIRECT:
            ret = new HTTPProxy(TYPE.DIRECT);
            if (ret.getHost() != null) {
                try {
                    final InetAddress ip = InetAddress.getByName(ret.getHost());
                    ret.setLocalIP(ip);
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
            break;
        case HTTP:
            ret = new HTTPProxy(TYPE.HTTP);
            ret.host = storable.getAddress();
            break;
        case SOCKS5:
            ret = new HTTPProxy(TYPE.SOCKS5);
            ret.host = storable.getAddress();
            break;
        }
        ret.setPass(storable.getPassword());
        ret.setUser(storable.getUsername());
        ret.setPort(storable.getPort());
        return ret;
    }

    private static String[] getInfo(final String host, final String port) {
        final String[] info = new String[2];
        if (host == null) { return info; }
        final String tmphost = host.replaceFirst("http://", "").replaceFirst("https://", "");
        String tmpport = new org.appwork.utils.Regex(host, ".*?:(\\d+)").getMatch(0);
        if (tmpport != null) {
            info[1] = "" + tmpport;
        } else {
            if (port != null) {
                tmpport = new Regex(port, "(\\d+)").getMatch(0);
            }
            if (tmpport != null) {
                info[1] = "" + tmpport;
            } else {
                Log.L.severe("No proxyport defined, using default 8080");
                info[1] = "8080";
            }
        }
        info[0] = new Regex(tmphost, "(.*?)(:|/|$)").getMatch(0);
        return info;
    }

    public static HTTPProxyStorable getStorable(final HTTPProxy proxy) {
        if (proxy == null || proxy.getType() == null) { return null; }
        final HTTPProxyStorable ret = new HTTPProxyStorable();
        switch (proxy.getType()) {
        case NONE:
            ret.setType(HTTPProxyStorable.TYPE.NONE);
            ret.setAddress(null);
            break;
        case DIRECT:
            ret.setType(HTTPProxyStorable.TYPE.DIRECT);
            if (proxy.getLocalIP() != null) {
                final String ip = proxy.getLocalIP().getHostAddress();
                ret.setAddress(ip);
            } else {
                ret.setAddress(null);
            }
            break;
        case HTTP:
            ret.setType(HTTPProxyStorable.TYPE.HTTP);
            ret.setAddress(proxy.getHost());
            break;
        case SOCKS5:
            ret.setType(HTTPProxyStorable.TYPE.SOCKS5);
            ret.setAddress(proxy.getHost());
            break;
        }
        ret.setPort(proxy.getPort());
        ret.setPassword(proxy.getPass());
        ret.setUsername(proxy.getUser());
        return ret;
    }

    private InetAddress localIP = null;

    private String      user    = null;

    private String      pass    = null;

    private int         port    = 80;

    protected String    host    = null;
    private TYPE        type    = TYPE.DIRECT;
    private STATUS      status  = STATUS.OK;

    public HTTPProxy(final InetAddress direct) {
        this.type = TYPE.DIRECT;
        this.localIP = direct;
    }

    public HTTPProxy(final TYPE type) {
        this.type = type;
    }

    public HTTPProxy(final TYPE type, final String host, final int port) {
        this.port = port;
        this.type = type;
        this.host = HTTPProxy.getInfo(host, "" + port)[0];
    }

    public AtomicInteger getCurrentConnections() {
        return this.currentConnections;
    }

    public String getHost() {
        return this.host;
    }

    /**
     * @return the localIP
     */
    public InetAddress getLocalIP() {
        return this.localIP;
    }

    public String getPass() {
        return this.pass;
    }

    public int getPort() {
        return this.port;
    }

    /**
     * @return the status
     */
    public STATUS getStatus() {
        return this.status;
    }

    public TYPE getType() {
        return this.type;
    }

    public AtomicLong getUsedConnections() {
        return this.usedConnections;
    }

    public String getUser() {
        return this.user;
    }

    /**
     * this proxy is DIRECT = using a local bound IP
     * 
     * @return
     */
    public boolean isDirect() {
        return this.type == TYPE.DIRECT;
    }

    public boolean isLocal() {
        return this.isDirect() || this.isNone();
    }

    /**
     * this proxy is NONE = uses default gateway
     * 
     * @return
     */
    public boolean isNone() {
        return this.type == TYPE.NONE;
    }

    /**
     * this proxy is REMOTE = using http,socks proxy
     * 
     * @return
     */
    public boolean isRemote() {
        return !this.isDirect() && !this.isNone();
    }

    public boolean sameProxy(final HTTPProxy proxy) {
        if (proxy == null) { return false; }
        if (this == proxy) { return true; }
        if (!proxy.getType().equals(this.type)) { return false; }
        if (proxy.getType().equals(TYPE.DIRECT)) {
            /* Direct Proxies only differ in IP */
            if (!proxy.getLocalIP().equals(this.localIP)) { return false; }
            return true;
        } else {
            if (!proxy.getHost().equalsIgnoreCase(this.host)) { return false; }
        }
        if (!proxy.getPass().equalsIgnoreCase(this.pass)) { return false; }
        if (!proxy.getUser().equalsIgnoreCase(this.user)) { return false; }
        if (proxy.getPort() != this.port) { return false; }
        return true;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * @param localIP
     *            the localIP to set
     */
    public void setLocalIP(final InetAddress localIP) {
        this.localIP = localIP;
    }

    public void setPass(final String pass) {
        this.pass = pass;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(final STATUS status) {
        this.status = status;
    }

    public void setType(final TYPE type) {
        this.type = type;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        if (this.type == TYPE.NONE) { return _AWU.T.proxy_none(); }
        if (this.type == TYPE.DIRECT) { return _AWU.T.proxy_direct(this.localIP.getHostAddress()); }
        if (this.type == TYPE.HTTP) { return _AWU.T.proxy_http(this.getHost(), this.getPort()); }
        if (this.type == TYPE.SOCKS5) { return _AWU.T.proxy_socks(this.getHost(), this.getPort()); }
        return "UNKNOWN";
    }
}
