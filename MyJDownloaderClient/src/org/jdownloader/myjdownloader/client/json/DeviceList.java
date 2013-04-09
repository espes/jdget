package org.jdownloader.myjdownloader.client.json;

import java.util.ArrayList;

public class DeviceList implements RequestIDValidator {

    private ArrayList<DeviceData> list;
    private long              rid;

    public ArrayList<DeviceData> getList() {
        return list;
    }

    public void setList(final ArrayList<DeviceData> list) {
        this.list = list;
    }

    public long getRid() {
        return rid;
    }

    public void setRid(final long rid) {
        this.rid = rid;
    }

    public DeviceList(/* storable */) {
        list = new ArrayList<DeviceData>();

    }

}
