package org.jdownloader.myjdownloader.client.json;

public class DisconnectResponse implements RequestIDValidator{

    private long rid = -1;

    public DisconnectResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return rid;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        rid = timestamp;
    }
}
