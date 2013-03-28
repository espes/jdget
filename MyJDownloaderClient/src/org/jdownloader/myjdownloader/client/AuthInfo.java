package org.jdownloader.myjdownloader.client;

public class AuthInfo {
    public static final Object STATUS_OK = "OK";

    public AuthInfo(/* "Keep empty Constructor for JSonStorage") */) {

    }

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRegaintoken() {
        return regaintoken;
    }

    public void setRegaintoken(String regaintoken) {
        this.regaintoken = regaintoken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private String regaintoken;
    private long   timestamp;

}
