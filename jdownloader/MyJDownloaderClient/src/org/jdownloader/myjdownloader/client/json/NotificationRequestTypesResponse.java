package org.jdownloader.myjdownloader.client.json;

public class NotificationRequestTypesResponse implements RequestIDValidator {

    private long                              rid   = -1;
    private NotificationRequestMessage.TYPE[] types = null;

    public NotificationRequestTypesResponse(/* Storable */) {
    }

    /**
     * @return the timestamp
     */
    public long getRid() {
        return this.rid;
    }

    /**
     * @return the types
     */
    public NotificationRequestMessage.TYPE[] getTypes() {
        return this.types;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setTypes(final NotificationRequestMessage.TYPE[] types) {
        this.types = types;
    }

}
