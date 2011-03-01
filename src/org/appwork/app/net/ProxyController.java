package org.appwork.app.net;

import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.Regex;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

class HttpAuthenticateProxy extends Authenticator {

    private final String pw;
    private final String user;

    public HttpAuthenticateProxy(final String user, final String pw) {
        this.user = user;
        this.pw = pw;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.pw.toCharArray());
    }
}

public class ProxyController {

    public enum PROXYTYPE {
        NONE,
        HTTP,
        SOCKS5
    }

    public static final String PROP_HOST = "PROXY_IP";
    public static final String PROP_PASS = "PROXY_AUTH_PASS";
    public static final String PROP_PORT = "PROXY_PORT";
    public static final String PROP_TYPE = "PROXY_TYPE";
    public static final String PROP_USER = "PROXY_AUTH_USER";

    public synchronized static void autoConfig() {
        /* reset proxy to none and lets find it */
        ProxyController.setType(PROXYTYPE.NONE);
        try {
            if (CrossSystem.isWindows()) {
                if (ProxyController.checkReg()) { return; }
            }
            /* we enable systemproxies to query them for a test getPage */
            System.setProperty("java.net.useSystemProxies", "true");

            List<Proxy> l;
            l = ProxySelector.getDefault().select(new URI("http://www.appwork.org"));

            for (final Proxy p : l) {
                final SocketAddress ad = p.address();
                if (ad != null && ad instanceof InetSocketAddress) {
                    final InetSocketAddress isa = (InetSocketAddress) ad;
                    if (isa.getHostName().trim().length() == 0) {
                        continue;
                    }
                    switch (p.type()) {
                    case HTTP:
                        ProxyController.setPort(isa.getPort());
                        ProxyController.setHost(isa.getHostName());
                        ProxyController.setType(PROXYTYPE.HTTP);
                        return;
                    case SOCKS:
                        ProxyController.setPort(isa.getPort());
                        ProxyController.setHost(isa.getHostName());
                        ProxyController.setType(PROXYTYPE.SOCKS5);
                        return;
                    }
                }
            }
        } catch (final Throwable e1) {
            Log.exception(Level.WARNING, e1);
        } finally {
            System.setProperty("java.net.useSystemProxies", "false");
            if (ProxyController.getType() != ProxyController.PROXYTYPE.NONE) {
                Log.L.info("Found Proxy: " + ProxyController.getHost() + ":" + ProxyController.getPort() + "(" + ProxyController.getType() + ")");
            }
        }
    }

    /**
     * Checks windows registry for proxy settings
     */
    private static boolean checkReg() {
        try {
            final Preferences userRoot = Preferences.userRoot();
            final Class<?> clz = userRoot.getClass();
            final Method openKey = clz.getDeclaredMethod("openKey", byte[].class, int.class, int.class);
            openKey.setAccessible(true);

            final Method closeKey = clz.getDeclaredMethod("closeKey", int.class);
            closeKey.setAccessible(true);
            final Method winRegQueryValue = clz.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            winRegQueryValue.setAccessible(true);

            byte[] valb = null;
            String vals = null;
            String key = null;
            Integer handle = -1;

            // Query Internet Settings for Proxy
            key = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
            try {
                handle = (Integer) openKey.invoke(userRoot, ProxyController.toCstr(key), 0x20019, 0x20019);
                valb = (byte[]) winRegQueryValue.invoke(userRoot, handle.intValue(), ProxyController.toCstr("ProxyServer"));
                vals = valb != null ? new String(valb).trim() : null;
            } finally {
                closeKey.invoke(Preferences.userRoot(), handle);
            }
            if (vals != null) {
                /* parse ip */
                String proxyurl = new Regex(vals, "(\\d+\\.\\d+\\.\\d+\\.\\d+)").getMatch(0);
                if (proxyurl == null) {
                    /* parse domain name */
                    proxyurl = new Regex(vals, "=(.*?)($|:)").getMatch(0);
                }
                final String port = new Regex(vals, ":(\\d+)").getMatch(0);
                if (proxyurl != null) {
                    if (vals.trim().contains("socks")) {
                        final int rPOrt = port != null ? Integer.parseInt(port) : 1080;
                        ProxyController.setPort(rPOrt);
                        ProxyController.setHost(proxyurl);
                        ProxyController.setType(PROXYTYPE.SOCKS5);
                        return true;
                    } else {
                        final int rPOrt = port != null ? Integer.parseInt(port) : 8080;
                        ProxyController.setPort(rPOrt);
                        ProxyController.setHost(proxyurl);
                        ProxyController.setType(PROXYTYPE.HTTP);
                        return true;
                    }
                }
            }
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }
        return false;
    }

    public static String getHost() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_HOST, "");
    }

    public static String getPassword() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PASS, "");
    }

    public static int getPort() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PORT, 8080);
    }

    public static PROXYTYPE getType() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_TYPE, ProxyController.PROXYTYPE.NONE);
    }

    public static String getUsername() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_USER, "");
    }

    public static void main(final String[] args) {
        ProxyController.autoConfig();
    }

    public static void setHost(final String text) {
        JSonStorage.getPlainStorage("Proxy").put(ProxyController.PROP_HOST, text);
    }

    private static void setHttp(final String host, final Integer portNum, final String user, final String pass) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", portNum.intValue() + "");

        System.setProperty("http.proxyUser", user);
        System.setProperty("http.proxyPassword", pass);
    }

    private static void setHttps(final String host, final Integer portNum, final String user, final String pass) {
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", portNum.intValue() + "");

        System.setProperty("http.proxyUser", user);
        System.setProperty("http.proxyPassword", pass);
    }

    public static void setPassword(final String text) {
        JSonStorage.getPlainStorage("Proxy").put(ProxyController.PROP_PASS, text);
    }

    public static void setPort(final Integer value) {
        JSonStorage.getPlainStorage("Proxy").put(ProxyController.PROP_PORT, value);
    }

    public static void setProxy() {
        try {
            final String host = JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_HOST, "");
            final int portNum = JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PORT, 8080);
            final String user = JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_USER, "");
            final String pass = JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PASS, "");
            final PROXYTYPE proxy = JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_TYPE, PROXYTYPE.NONE);
            switch (proxy) {
            case HTTP:
                ProxyController.setHttp(host, portNum, user, pass);
                ProxyController.setHttps(host, portNum, user, pass);
                Authenticator.setDefault(new HttpAuthenticateProxy(user, pass));
                break;
            case SOCKS5:
                ProxyController.setSocks(host, portNum, user, pass);
                Authenticator.setDefault(new HttpAuthenticateProxy(user, pass));
                break;
            default:
                System.setProperty("http.proxyHost", "");
                System.setProperty("https.proxyHost", "");
                System.setProperty("socksProxyHost", "");
                Authenticator.setDefault(null);
                break;
            }
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }
    }

    private static void setSocks(final String host, final Integer portNum, final String user, final String pass) {
        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", portNum.intValue() + "");

        System.setProperty("http.proxyUser", user);
        System.setProperty("http.proxyPassword", pass);
    }

    public static void setType(final PROXYTYPE selectedItem) {
        JSonStorage.getPlainStorage("Proxy").put(ProxyController.PROP_TYPE, selectedItem);
    }

    public static void setUsername(final String text) {
        JSonStorage.getPlainStorage("Proxy").put(ProxyController.PROP_USER, text);
    }

    private static byte[] toCstr(final String str) {
        final byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }

}
