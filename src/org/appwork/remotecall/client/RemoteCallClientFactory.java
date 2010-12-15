package org.appwork.remotecall.client;

import java.lang.reflect.Proxy;

public class RemoteCallClientFactory {

    private final RemoteCallClient client;

    public RemoteCallClientFactory(final RemoteCallClient remoteCallClient) {
        client = remoteCallClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(final Class<T> class1) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { class1 }, new InvocationHandlerImpl(client, class1));
    }

}
