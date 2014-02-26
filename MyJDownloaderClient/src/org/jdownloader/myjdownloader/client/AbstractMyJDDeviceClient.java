package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfo;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfos;

public class AbstractMyJDDeviceClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    private final String                          deviceID;
    private DirectConnectionInfo                  connection = null;
    
    public AbstractMyJDDeviceClient(final String deviceID, final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
        this.deviceID = deviceID;
    }
    
    public Object callAction(final String action, final GenericType returnType, final Object... args) throws MyJDownloaderException, APIException {
        final DirectConnectionInfo lconnection = this.connection;
        String host = null;
        if (lconnection != null) {
            host = "http://" + lconnection.getIp() + ":" + lconnection.getPort();
        }
        return this.api.callAction(host, this.getDeviceID(), action, returnType, args);
    }
    
    public String getDeviceID() {
        return this.deviceID;
    }
    
    public DirectConnectionInfo getDirectConnectionInfo() {
        return this.connection;
    }
    
    public DirectConnectionInfos getDirectConnectionInfos() throws MyJDownloaderException, APIException {
        return this.api.getDirectConnectionInfos(this.getDeviceID());
    }
    
    public void setDirectConnectionInfo(final DirectConnectionInfo connection) {
        this.connection = connection;
    }
    
    public boolean verifyDirectConnectionInfo(final DirectConnectionInfo connection) throws MyJDownloaderException, APIException {
        if (connection == null) { return false; }
        return this.api.verifyDirectConnectionInfo(this.getDeviceID(), connection);
    }
}
