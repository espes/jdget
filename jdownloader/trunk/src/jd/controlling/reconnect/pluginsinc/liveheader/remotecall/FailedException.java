package jd.controlling.reconnect.pluginsinc.liveheader.remotecall;

import org.appwork.remotecall.server.RemoteCallException;

public class FailedException extends RemoteCallException {

    /**
     * 
     */
    public FailedException() {
        super();
        
    }

    /**
     * @param string
     */
    public FailedException(String string) {
        super(string);
        
    }

}
