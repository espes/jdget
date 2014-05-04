package org.jdownloader.myjdownloader.client.json;

public class MyCaptchaChallenge {
    
    public static enum TYPE {
        TEXT
    }
    
    private TYPE   type    = TYPE.TEXT;
    private String source  = null;
    private String dataURL = null;
    private String id      = null;
    
    public MyCaptchaChallenge(/* Storable */) {
    }
    
    public String getDataURL() {
        return this.dataURL;
    }
    
    public final String getId() {
        return this.id;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public TYPE getType() {
        return this.type;
    }
    
    public void setDataURL(final String dataURL) {
        this.dataURL = dataURL;
    }
    
    public final void setId(final String id) {
        this.id = id;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public void setType(final TYPE type) {
        this.type = type;
    }
}
