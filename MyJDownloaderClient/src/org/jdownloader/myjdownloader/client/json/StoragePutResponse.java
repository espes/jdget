package org.jdownloader.myjdownloader.client.json;

public class StoragePutResponse implements RequestIDValidator {
    private long rid    = -1;

    private long itemID = -1;

    public StoragePutResponse(/* Storable */) {
    }

    public long getItemID() {
        return this.itemID;
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    public void setItemID(final long itemID) {
        this.itemID = itemID;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

}
