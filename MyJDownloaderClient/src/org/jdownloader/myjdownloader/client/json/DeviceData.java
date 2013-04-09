package org.jdownloader.myjdownloader.client.json;

public class DeviceData {
    private String id;

    private String type;
    private long   lastConnection = -1;
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
     * @return the lastConnection
     */
    public long getLastConnection() {
        return this.lastConnection;
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
     * @param lastConnection
     *            the lastConnection to set
     */
    public void setLastConnection(final long lastConnection) {
        this.lastConnection = lastConnection;
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
