package org.jdownloader.myjdownloader.client.json;


public class DeviceConnectResponse implements RequestIDValidator {

 
    private long   rid         = -1;
    private String deviceid    = null;

    public DeviceConnectResponse(/* Storable */) {
    }

    public String getDeviceid() {
        return deviceid;
    }

 
    public long getRid() {
        return rid;
    }

    public void setDeviceid(final String deviceid) {
        this.deviceid = deviceid;
    }
   

    public void setRid(final long rid) {
        this.rid = rid;
    }

}
