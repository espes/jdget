package org.jdownloader.myjdownloader.client;

public class SessionInfo {

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (this.getClass() != o.getClass()) { return false; }
        return ((SessionInfo) o).getSessionToken().equals(this.getSessionToken());
    }

    @Override
    public int hashCode() {
        return this.sessionToken.hashCode();
    }

    public byte[] getDeviceEncryptionToken() {
        return this.deviceEncryptionToken;
    }

    public void setDeviceEncryptionToken(final byte[] deviceEncryptionToken) {
        this.deviceEncryptionToken = deviceEncryptionToken;
    }

    public byte[] getDeviceSecret() {
        return this.deviceSecret;
    }

    public void setDeviceSecret(final byte[] deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getRegainToken() {
        return this.regainToken;
    }

    public void setRegainToken(final String regainToken) {
        this.regainToken = regainToken;
    }

    public byte[] getServerEncryptionToken() {
        return this.serverEncryptionToken;
    }

    public void setServerEncryptionToken(final byte[] serverEncryptionToken) {
        this.serverEncryptionToken = serverEncryptionToken;
    }

    public String getSessionToken() {
        return this.sessionToken;
    }

    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

}
