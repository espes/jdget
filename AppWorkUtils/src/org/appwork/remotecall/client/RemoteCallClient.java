package org.appwork.remotecall.client;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import org.appwork.remotecall.Utils;
import org.appwork.remotecall.server.ServerInvokationException;

public abstract class RemoteCallClient {

    private final RemoteCallClientFactory factory;

    public RemoteCallClient() {

        factory = new RemoteCallClientFactory(this);

    }

    
    /**
     * @param name
     * @param method
     * @param args
     * @return
     */
    public Object call(String serviceName, Method method, Object[] args)  throws ServerInvokationException {
        try {
            return send(serviceName, method, Utils.serialise(args));
        } catch (final SerialiseException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
   

    /**
     * @param serviceName
     * @param routine
     * @param args
     * @return
     */

    public RemoteCallClientFactory getFactory() {
        return factory;
    }

    /**
     * @param serviceName
     * @param routine
     * @param serialise
     * @return
     * @throws ServerInvokationException
     * @throws Exception
     */
    protected abstract Object send(String serviceName, Method routine, String serialise) throws ServerInvokationException;



}
