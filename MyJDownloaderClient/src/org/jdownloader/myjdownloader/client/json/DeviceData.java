package org.jdownloader.myjdownloader.client.json;

public class DeviceData {
    private String id;

    private String type;
    private long   lastSeenTimestamp = -1;
    private String name;

    public DeviceData(/* storable */) {

    }

    public DeviceData(final String deviceid, final String deviceType, final String deviceName) {
        this.id = deviceid;
        this.type = deviceType;
        this.name = deviceName;
    }

    public String getId() {
        return this.id;
    }

    /**
     * @return the lastSeenTimestamp
     */
    public long getLastSeenTimestamp() {
        return this.lastSeenTimestamp;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param lastSeenTimestamp
     *            the lastSeenTimestamp to set
     */
    public void setLastSeenTimestamp(final long lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.id + ")";
    }

}
