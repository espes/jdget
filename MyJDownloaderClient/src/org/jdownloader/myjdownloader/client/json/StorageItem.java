package org.jdownloader.myjdownloader.client.json;

public class StorageItem {

    private String name      = null;
    private String content   = null;
    private long   timestamp = System.currentTimeMillis();

    public StorageItem(/* Storable */) {
    }

    public String getContent() {
        return this.content;
    }

    public String getName() {
        return this.name;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

}
