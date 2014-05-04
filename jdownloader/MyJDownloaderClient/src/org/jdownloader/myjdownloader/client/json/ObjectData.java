package org.jdownloader.myjdownloader.client.json;

public class ObjectData implements RequestIDValidator {
    private long   rid = -1;
    
    private Object data;
    
    public ObjectData(/* keep empty constructor json */) {
        
    }
    
    public Object getData() {
        return this.data;
    }
    
    public long getRid() {
        return this.rid;
    }
    
    public void setData(final Object data) {
        this.data = data;
    }
    
    public void setRid(final long rid) {
        this.rid = rid;
    }
}
