package org.appwork.remotecall.exceptions;

import org.appwork.remotecall.server.RemoteCallException;

public class IPNotAllowedException extends RemoteCallException {

    /**
     * 
     */
    private static final long serialVersionUID = -5165495572484252835L;
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
