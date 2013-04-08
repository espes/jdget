package org.jdownloader.myjdownloader.server.json;

import org.jdownloader.myjdownloader.client.json.RequestIDValidator;

public class DeviceConnectResponse implements RequestIDValidator {

    private String devicetoken = null;
    private long   rid         = -1;
    private String deviceid    = null;

    public DeviceConnectResponse(/* Storable */) {
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public String getDevicetoken() {
        return this.devicetoken;
    }

    public long getRid() {
        return this.rid;
    }

    public void setDeviceid(final String deviceid) {
        this.deviceid = deviceid;
    }

    public void setDevicetoken(final String devicetoken) {
        this.devicetoken = devicetoken;
    }

    public void setRid(final long rid) {
        this.rid = rid;
    }

}
