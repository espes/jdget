package org.jdownloader.myjdownloader.client.json;

public class ConnectResponse implements RequestIDValidator {

    private String sessiontoken = null;
    private String regaintoken  = "blablablb";
    private long   rid          = -1;

    public ConnectResponse(/* Storable */) {
    }

    public String getRegaintoken() {
        return this.regaintoken;
    }

    public long getRid() {
        return this.rid;
    }

    public String getSessiontoken() {
        return this.sessiontoken;
    }

    public void setRegaintoken(final String regaintoken) {
        this.regaintoken = regaintoken;
    }

    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

    public void setSessiontoken(final String token) {
        this.sessiontoken = token;
    }

}
