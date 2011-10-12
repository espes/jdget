package org.appwork.remotecall.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.appwork.remotecall.RemoteCallInterface;
import org.appwork.remotecall.client.InvocationHandlerImpl;
import org.appwork.remotecall.client.MethodHandler;

public class RemoteCallServiceWrapper {

    private final Object                   _service;
    private HashMap<String, MethodHandler> handler;

    public <T extends RemoteCallInterface> RemoteCallServiceWrapper(Class<T> class1, T serviceImpl) throws ParsingException {
        _service = serviceImpl;

        handler = new InvocationHandlerImpl<T>(null, class1).getHandler();

    }

    /**
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public Object call(final Method m, final Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return m.invoke(_service, params);
    }

    /**
     * @param method
     * @return
     */
    public MethodHandler getHandler(String method) {
       
        return handler.get(method);
    }

   
}
