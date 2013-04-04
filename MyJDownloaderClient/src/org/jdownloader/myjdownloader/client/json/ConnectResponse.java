package org.jdownloader.myjdownloader.client.json;

public class ConnectResponse implements TimestampValidator {

    private String token       = null;
    private String regaintoken = null;
    private long   timestamp   = -1;

    public ConnectResponse(/* Storable */) {
    }

    public String getRegaintoken() {
        return this.regaintoken;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getToken() {
        return this.token;
    }

    public void setRegaintoken(final String regaintoken) {
        this.regaintoken = regaintoken;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setToken(final String token) {
        this.token = token;
    }

}
