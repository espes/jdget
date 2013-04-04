package org.jdownloader.myjdownloader.client.json;


public class ConnectResponse {

    public static enum STATUS {
        OK,
        AWAITING_EMAIL_CONFIRMATION
    }

    private String token       = null;
    private String regaintoken = null;
    private long   timestamp   = -1;
    private STATUS status      = STATUS.OK;

    public ConnectResponse(/* Storable */) {
    }

    public String getRegaintoken() {
        return regaintoken;
    }

    public STATUS getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setRegaintoken(final String regaintoken) {
        this.regaintoken = regaintoken;
    }

    public void setStatus(final STATUS status) {
        this.status = status;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setToken(final String token) {
        this.token = token;
    }

}
