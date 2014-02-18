package org.jdownloader.myjdownloader.client.json;

public class AccessTokenResponse implements RequestIDValidator {
    
    private long   rid          = -1;
    private String accessToken  = null;
    private String accessSecret = null;
    
    public AccessTokenResponse(/* Storable */) {
    }
    
    /**
     * @return the accessSecret
     */
    public String getAccessSecret() {
        return this.accessSecret;
    }
    
    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return this.accessToken;
    }
    
    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }
    
    /**
     * @param accessSecret
     *            the accessSecret to set
     */
    public void setAccessSecret(final String accessSecret) {
        this.accessSecret = accessSecret;
    }
    
    /**
     * @param accessToken
     *            the accessToken to set
     */
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }
    
    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }
}
