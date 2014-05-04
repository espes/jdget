package org.jdownloader.myjdownloader.client.json.storage;

import java.util.Map;

import org.jdownloader.myjdownloader.client.json.RequestIDValidator;

public class StorageListResponse implements RequestIDValidator {
    private long              rid  = -1;

    private Map<String, Long> list = null;

    public StorageListResponse(/* Storable */) {
    }

    public Map<String, Long> getList() {
        return this.list;
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    public void setList(final Map<String, Long> list) {
        this.list = list;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

}
