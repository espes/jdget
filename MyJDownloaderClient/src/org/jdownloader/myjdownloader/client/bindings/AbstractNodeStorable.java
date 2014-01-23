package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public abstract class AbstractNodeStorable extends AbstractJsonData implements Node {

    private long    bytesTotal = -1;
    private String  comment    = null;
    private boolean enabled    = false;
    private String  name       = null;
    private long    uuid       = -1;

    public long getBytesTotal() {
        return bytesTotal;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public long getUuid() {
        return uuid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setBytesTotal(final long size) {
        bytesTotal = size;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

}
