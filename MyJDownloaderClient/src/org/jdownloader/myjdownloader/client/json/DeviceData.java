package org.jdownloader.myjdownloader.client.json;

public class DeviceData {
    public DeviceData(/* storable */) {

    }

    private String id;
    private String type;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
    public String toString(){
        return name+" ("+id+")";
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private String name;

    public DeviceData(final String deviceid, final String deviceType, final String deviceName) {
        id = deviceid;
        type = deviceType;
        name = deviceName;
    }

}
