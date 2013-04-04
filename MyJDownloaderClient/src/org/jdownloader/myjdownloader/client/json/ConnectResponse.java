package org.jdownloader.myjdownloader.client.json;

public class ConnectResponse implements RequestIDValidator {

    private String token       = null;
    private String regaintoken = null;
    private long   rid   = -1;

    public ConnectResponse(/* Storable */) {
    }

    public String getRegaintoken() {
        return regaintoken;
    }

    public long getRid() {
        return rid;
    }

    public String getToken() {
        return token;
    }

    public void setRegaintoken(final String regaintoken) {
        this.regaintoken = regaintoken;
    }

    public void setRid(final long timestamp) {
        rid = timestamp;
    }

    public void setToken(final String token) {
        this.token = token;
    }

}
