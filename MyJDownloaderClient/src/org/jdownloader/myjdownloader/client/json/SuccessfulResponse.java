package org.jdownloader.myjdownloader.client.json;

public class SuccessfulResponse {

    private boolean successful = false;
    private long    timestamp  = -1;

    public SuccessfulResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return this.successful;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(final boolean successful) {
        this.successful = successful;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
}
