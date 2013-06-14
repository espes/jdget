package org.jdownloader.myjdownloader.client.json;

public class NotificationMessage {

    private Object data        = null;
    private String collapseKey = null;

    public NotificationMessage(/* Storable */) {
    }

    public String getCollapseKey() {
        return this.collapseKey;
    }

    public Object getData() {
        return this.data;
    }

    public void setCollapseKey(final String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public void setData(final Object data) {
        this.data = data;
    }

}
