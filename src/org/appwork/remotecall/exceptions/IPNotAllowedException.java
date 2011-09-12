package org.appwork.remotecall.exceptions;

import org.appwork.remotecall.server.RemoteCallException;
import org.appwork.remotecall.server.Requestor;

public class IPNotAllowedException extends RemoteCallException {

    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public IPNotAllowedException(){
        
        //
        super();
    }
    /**
     * @param string
     * @param ip 
     */
    public IPNotAllowedException(String string, String ip) {
       super(string);
       this.ip=ip;

    }

}
