package org.jdownloader.myjdownloader.client.json;

public class CaptchaResponse extends RequestIDOnly {
    
    public static enum STATE {
        QUEUED,
        PROCESSING,
        SOLVED,
        FAILED,
        NOT_AVAILABLE
    }
    
    private String id       = null;
    private STATE  state    = STATE.QUEUED;
    private String response = null;
    
    public CaptchaResponse(/* Storable */) {
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getResponse() {
        return this.response;
    }
    
    public STATE getState() {
        return this.state;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setResponse(final String response) {
        this.response = response;
    }
    
    public void setState(final STATE state) {
        this.state = state;
    }
}
