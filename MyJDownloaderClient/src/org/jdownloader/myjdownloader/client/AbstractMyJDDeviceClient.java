package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.DeviceData;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfo;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfos;

public class AbstractMyJDDeviceClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    private final DeviceData                      deviceData;
    private DirectConnectionInfo                  connection = null;
    
    public AbstractMyJDDeviceClient(final DeviceData deviceData, final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
        this.deviceData = deviceData;
    }
    
    public Object callAction(final String action, final GenericType returnType, final Object... args) throws MyJDownloaderException, APIException {
        final DirectConnectionInfo lconnection = this.connection;
        String host = null;
        if (lconnection != null) {
            host = "http://" + lconnection.getIp() + ":" + lconnection.getPort();
        }
        return this.api.callAction(host, this.deviceData.getId(), action, returnType, args);
    }
    
    public DeviceData getDeviceData() {
        return this.deviceData;
    }
    
    public String getDeviceID() {
        return this.deviceData.getId();
    }
    
    public DirectConnectionInfo getDirectConnectionInfo() {
        return this.connection;
    }
    
    public DirectConnectionInfos getDirectConnectionInfos() throws MyJDownloaderException, APIException {
        return this.api.getDirectConnectionInfos(this.deviceData.getId());
    }
    
    public void setDirectConnectionInfo(final DirectConnectionInfo connection) {
        this.connection = connection;
    }
    
    public boolean verifyDirectConnectionInfo(final DirectConnectionInfo connection) throws MyJDownloaderException, APIException {
        if (connection == null) { return false; }
        return this.api.verifyDirectConnectionInfo(this.deviceData.getId(), connection);
    }
}
