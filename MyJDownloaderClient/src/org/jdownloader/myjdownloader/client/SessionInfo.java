package org.jdownloader.myjdownloader.client;

public class SessionInfo {

    private byte[] deviceSecret;
    private byte[] deviceEncryptionToken;

    public byte[] getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(final byte[] deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public byte[] getDeviceEncryptionToken() {
        return deviceEncryptionToken;
    }

    public void setDeviceEncryptionToken(final byte[] deviceEncryptionToken) {
        this.deviceEncryptionToken = deviceEncryptionToken;
    }

    public byte[] getServerEncryptionToken() {
        return serverEncryptionToken;
    }

    public void setServerEncryptionToken(final byte[] serverEncryptionToken) {
        this.serverEncryptionToken = serverEncryptionToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getRegainToken() {
        return regainToken;
    }

    public void setRegainToken(final String regainToken) {
        this.regainToken = regainToken;
    }

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

}
