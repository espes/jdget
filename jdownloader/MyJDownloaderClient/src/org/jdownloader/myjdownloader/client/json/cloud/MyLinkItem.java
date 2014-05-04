package org.jdownloader.myjdownloader.client.json.cloud;

public class MyLinkItem {
    public static enum CONTENTTYPE {
        NA,
        UNKNOWN,
        PLAINTEXT,
        FILE,
        CNL2
    }
    
    private String  contentType = CONTENTTYPE.NA.name();
    
    private long    timestamp   = -1;
    
    private String  content     = null;
    
    private String  id          = null;
    
    private String  title       = null;
    private String  source      = null;
    
    private String  deviceID    = null;
    private Boolean pin         = null;
    
    public MyLinkItem(/* storable */) {
    }
    
    public CONTENTTYPE _getContentType() {
        try {
            if (this.contentType == null) { return CONTENTTYPE.NA; }
            return CONTENTTYPE.valueOf(this.contentType);
        } catch (final Throwable e) {
            return CONTENTTYPE.UNKNOWN;
        }
    }
    
    public void _setContentType(CONTENTTYPE contentType) {
        if (contentType == null) {
            contentType = CONTENTTYPE.NA;
        }
        this.contentType = contentType.name();
    }
    
    public String getContent() {
        return this.content;
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public String getDeviceID() {
        return this.deviceID;
    }
    
    public String getId() {
        return this.id;
    }
    
    public Boolean getPin() {
        return this.pin;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setContent(final String content) {
        this.content = content;
    }
    
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    public void setDeviceID(final String deviceID) {
        this.deviceID = deviceID;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setPin(final Boolean pin) {
        this.pin = pin;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
}
