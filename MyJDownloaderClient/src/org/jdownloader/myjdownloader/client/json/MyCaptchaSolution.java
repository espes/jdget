package org.jdownloader.myjdownloader.client.json;

public class MyCaptchaSolution {
    
    public static enum RESULT {
        CORRECT,
        WRONG,
        TIMEOUT,
        ABORT
    }
    
    public static enum STATE {
        QUEUED,
        PROCESSING,
        SOLVED,
        NOT_AVAILABLE
    }
    
    private String id       = null;
    private STATE  state    = STATE.NOT_AVAILABLE;
    private String response = null;
    
    public MyCaptchaSolution(/* Storable */) {
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
