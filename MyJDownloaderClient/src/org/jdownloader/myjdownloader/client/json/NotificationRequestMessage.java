package org.jdownloader.myjdownloader.client.json;

public class NotificationRequestMessage {

    public static enum REQUESTTYPE {
        CAPTCHA
    }

    private long        timestamp = System.currentTimeMillis();
    private REQUESTTYPE type      = REQUESTTYPE.CAPTCHA;
    private boolean     requested = false;

    public NotificationRequestMessage(/* Storable */) {
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public REQUESTTYPE getType() {
        return this.type;
    }

    /**
     * @return the requested
     */
    public boolean isRequested() {
        return this.requested;
    }

    /**
     * @param requested
     *            the requested to set
     */
    public void setRequested(final boolean requested) {
        this.requested = requested;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(final REQUESTTYPE type) {
        this.type = type;
    }
}
