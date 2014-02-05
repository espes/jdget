package org.jdownloader.myjdownloader.client.json;

public class CryptedStorageItem {

    private String cryptedName    = null;

    private String cryptedContent = null;

    private long   timestamp      = -1;

    private String sessionToken   = null;

    public CryptedStorageItem(/* Storable */) {

    }

    public String getCryptedContent() {
        return this.cryptedContent;
    }

    public String getCryptedName() {
        return this.cryptedName;
    }

    public String getSessionToken() {
        return this.sessionToken;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setCryptedContent(final String cryptedContent) {
        this.cryptedContent = cryptedContent;
    }

    public void setCryptedName(final String cryptedName) {
        this.cryptedName = cryptedName;
    }

    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

}
