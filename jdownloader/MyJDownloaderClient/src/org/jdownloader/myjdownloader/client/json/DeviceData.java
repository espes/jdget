package org.jdownloader.myjdownloader.client.json;

public class DeviceData {
    private String id;

    private String type;
    private String name;

    public DeviceData(/* storable */) {

    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeviceData)) {
            return false;
        }
        if (!equalsString(id, ((DeviceData) obj).id)) {
            return false;
        }
        if (!equalsString(type, ((DeviceData) obj).type)) {
            return false;
        }
        if (!equalsString(name, ((DeviceData) obj).name)) {
            return false;
        }
        return true;
    }

    public static boolean equalsString(final String pass, final String pass2) {
        if (pass == pass2) { return true; }
        if (pass == null && pass2 != null) { return false; }
        return pass.equals(pass2);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    public DeviceData(final String deviceid, final String deviceType, final String deviceName) {
        id = deviceid;
        type = deviceType;
        name = deviceName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

}
