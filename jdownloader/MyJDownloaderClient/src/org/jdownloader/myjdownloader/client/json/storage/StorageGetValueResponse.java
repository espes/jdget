package org.jdownloader.myjdownloader.client.json.storage;

import org.jdownloader.myjdownloader.client.json.RequestIDValidator;

public class StorageGetValueResponse implements RequestIDValidator {
    private long   rid = -1;

    private String value;

    public StorageGetValueResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

    public void setValue(final String value) {
        this.value = value;
    }

}
