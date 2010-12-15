package org.appwork.remotecall.client;

import org.appwork.remotecall.Utils;

public abstract class RemoteCallClient {

    private final RemoteCallClientFactory factory;

    public RemoteCallClient() {

        factory = new RemoteCallClientFactory(this);

    }

    public String call(final String serviceName, final String routine, final Object[] args) throws Exception {

        return send(serviceName, routine, Utils.serialise(args));

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
     * @throws Exception
     */
    protected abstract String send(String serviceName, String routine, String serialise) throws Exception;

}
