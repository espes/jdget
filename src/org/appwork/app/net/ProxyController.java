package org.appwork.app.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.appwork.storage.JSonStorage;

class HttpAuthenticateProxy extends Authenticator {

    private final String pw;
    private final String user;

    public HttpAuthenticateProxy(final String user, final String pw) {
        this.user = user;
        this.pw = pw;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pw.toCharArray());
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

    public static String getHost() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_HOST, "");
    }

    public static String getPassword() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PASS, "");
    }

    public static int getPort() {
        // 8080
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_PORT, 8080);
    }

    public static PROXYTYPE getType() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_TYPE, ProxyController.PROXYTYPE.NONE);
    }

    public static String getUsername() {
        return JSonStorage.getPlainStorage("Proxy").get(ProxyController.PROP_USER, "");
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

}
