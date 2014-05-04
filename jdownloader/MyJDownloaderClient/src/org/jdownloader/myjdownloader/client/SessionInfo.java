package org.jdownloader.myjdownloader.client;

import java.util.Arrays;

public class SessionInfo {

    private static final int NULLHASHCODE = "EMPTYSESIONTOKEN".hashCode();

    private static boolean equals(final String a, final String b) {
        return a == b || a != null && a.equals(b);
    }

    private byte[] deviceSecret;
    private byte[] deviceEncryptionToken;
    private byte[] serverEncryptionToken;
    private String sessionToken;

    private String regainToken;

    public SessionInfo(/* STorable */) {

    }

    public SessionInfo(final byte[] deviceSecret, final byte[] serverEncryptionToken, final byte[] deviceEncryptionToken, final String sessionToken, final String regainToken) {
        this.deviceSecret = deviceSecret;
        this.deviceEncryptionToken = deviceEncryptionToken;
        this.serverEncryptionToken = serverEncryptionToken;
        this.sessionToken = sessionToken;
        this.regainToken = regainToken;
    }

    @Override
    public boolean equals(final Object o) {
        boolean equals = this == o;
        equals = equals || o != null;
        equals = equals && this.getClass() == o.getClass();
        final SessionInfo other = (SessionInfo) o;
        equals = equals && Arrays.equals(this.getDeviceSecret(), other.getDeviceSecret());
        equals = equals && Arrays.equals(this.getDeviceEncryptionToken(), other.getDeviceEncryptionToken());
        equals = equals && Arrays.equals(this.getServerEncryptionToken(), other.getServerEncryptionToken());
        equals = equals && SessionInfo.equals(this.getSessionToken(), other.getSessionToken());
        equals = equals && SessionInfo.equals(this.getRegainToken(), other.getRegainToken());
        return equals;
    }

    public byte[] getDeviceEncryptionToken() {
        return this.deviceEncryptionToken;
    }

    public byte[] getDeviceSecret() {
        return this.deviceSecret;
    }

    public String getRegainToken() {
        return this.regainToken;
    }

    public byte[] getServerEncryptionToken() {
        return this.serverEncryptionToken;
    }

    public String getSessionToken() {
        return this.sessionToken;
    }

    @Override
    public int hashCode() {
        final String lsessionToken = this.sessionToken;
        if (lsessionToken != null) { return lsessionToken.hashCode(); }
        return SessionInfo.NULLHASHCODE;
    }

    public void setDeviceEncryptionToken(final byte[] deviceEncryptionToken) {
        this.deviceEncryptionToken = deviceEncryptionToken;
    }

    public void setDeviceSecret(final byte[] deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public void setRegainToken(final String regainToken) {
        this.regainToken = regainToken;
    }

    public void setServerEncryptionToken(final byte[] serverEncryptionToken) {
        this.serverEncryptionToken = serverEncryptionToken;
    }

    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

}
