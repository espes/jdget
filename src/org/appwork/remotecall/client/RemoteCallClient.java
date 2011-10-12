package org.appwork.remotecall.client;

import java.io.UnsupportedEncodingException;

import org.appwork.remotecall.Utils;
import org.appwork.remotecall.server.ServerInvokationException;

public abstract class RemoteCallClient {

    private final RemoteCallClientFactory factory;

    public RemoteCallClient() {

        factory = new RemoteCallClientFactory(this);

    }

    public String call(final String serviceName, final String routine, final Object[] args) throws ServerInvokationException {

        try {
            return send(serviceName, routine, Utils.serialise(args));
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
    protected abstract String send(String serviceName, String routine, String serialise) throws ServerInvokationException;

}
