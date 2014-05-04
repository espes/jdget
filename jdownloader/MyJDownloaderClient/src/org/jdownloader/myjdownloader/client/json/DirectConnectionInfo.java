package org.jdownloader.myjdownloader.client.json;

public class DirectConnectionInfo {
    
    private int    port = -1;
    
    private String ip   = null;
    
    public DirectConnectionInfo(/* Storable */) {
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void setIp(final String ip) {
        this.ip = ip;
    }
    
    public void setPort(final int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        return this.ip + ":" + this.port;
    }
    
}
