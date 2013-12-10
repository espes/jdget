package org.jdownloader.myjdownloader.client.json;

public class ObjectData implements RequestIDValidator {
    public ObjectData(/* keep empty constructor json */) {

    }

    private long rid = -1;

    public long getRid() {
        return rid;
    }

    public void setRid(final long rid) {
        this.rid = rid;
    }

    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }
}
