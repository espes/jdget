package org.jdownloader.myjdownloader.client.json;

public class RequestIDOnly implements RequestIDValidator {

    private long rid = -1;

    public RequestIDOnly(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }
}
