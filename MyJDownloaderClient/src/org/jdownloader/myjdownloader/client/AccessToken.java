package org.jdownloader.myjdownloader.client;

public class AccessToken {
    private final String accessToken;
    private final String accessSecret;
    
    public AccessToken(final String accessToken, final String accessSecret) {
        this.accessSecret = accessSecret;
        this.accessToken = accessToken;
    }
    
    public String getAccessSecret() {
        return this.accessSecret;
    }
    
    public String getAccessToken() {
        return this.accessToken;
    }
}
