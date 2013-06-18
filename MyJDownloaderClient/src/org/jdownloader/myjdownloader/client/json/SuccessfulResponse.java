package org.jdownloader.myjdownloader.client.json;

public class SuccessfulResponse implements RequestIDValidator {
    private long    rid        = -1;

    private boolean successful = false;

    public SuccessfulResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return this.successful;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(final boolean successful) {
        this.successful = successful;
    }

}
