package org.jdownloader.myjdownloader.client.json;

public class DisconnectResponse {

    private long timestamp = -1;

    public DisconnectResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
}
