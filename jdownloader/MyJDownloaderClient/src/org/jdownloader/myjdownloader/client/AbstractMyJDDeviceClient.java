package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.exceptions.APIException;
import org.jdownloader.myjdownloader.client.exceptions.DirectConnectionException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.UnexpectedIOException;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfo;
import org.jdownloader.myjdownloader.client.json.DirectConnectionInfos;

public class AbstractMyJDDeviceClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    private final String                          deviceID;
    private DirectConnectionInfo                  connection   = null;
    private boolean                               autoFallback = true;
    
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
        try {
            return this.api.callAction(host, this.getDeviceID(), action, returnType, args);
        } catch (final MyJDownloaderException e) {
            if (this.onDirectConnectionException(lconnection, e)) {
                return this.api.callAction(null, this.getDeviceID(), action, returnType, args);
            } else {
                throw e;
            }
        }
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
    
    public boolean isAutoFallbackEnabled() {
        return this.autoFallback;
    }
    
    protected boolean onDirectConnectionException(final DirectConnectionInfo directConnectionInfo, final MyJDownloaderException e) throws MyJDownloaderException {
        if (directConnectionInfo != null && e instanceof UnexpectedIOException) {
            if (this.isAutoFallbackEnabled() == false) {
                Throwable cause = e;
                Exception rootCause = e;
                while ((cause = cause.getCause()) != null) {
                    if (cause instanceof Exception) {
                        rootCause = (Exception) cause;
                    } else {
                        break;
                    }
                }
                throw new DirectConnectionException(rootCause);
            }
            return true;
        }
        return false;
    }
    
    public void setAutoFallbackEnabled(final boolean autoFallback) {
        this.autoFallback = autoFallback;
    }
    
    public void setDirectConnectionInfo(final DirectConnectionInfo connection) {
        this.connection = connection;
    }
    
    public boolean verifyDirectConnectionInfo(final DirectConnectionInfo connection) throws MyJDownloaderException, APIException {
        if (connection == null) { return false; }
        return this.api.verifyDirectConnectionInfo(this.getDeviceID(), connection);
    }
}
