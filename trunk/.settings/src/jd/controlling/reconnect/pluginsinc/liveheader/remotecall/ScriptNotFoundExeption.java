package jd.controlling.reconnect.pluginsinc.liveheader.remotecall;

import org.appwork.remotecall.server.RemoteCallException;

public class ScriptNotFoundExeption extends RemoteCallException {

    public ScriptNotFoundExeption(String string) {
        super(string);
    }

    /**
     * 
     */
    public ScriptNotFoundExeption() {
        super();
        
    }

}
