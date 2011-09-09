package org.appwork.remotecall.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.appwork.remotecall.Utils;

public class RemoteCallServiceWrapper {

    public static RemoteCallServiceWrapper create(final Object serviceImpl) {
        final RemoteCallServiceWrapper ret = new RemoteCallServiceWrapper(serviceImpl);
        return ret;
    }

    private final Object                  _service;
    private final Method[]                methods;
    private final HashMap<String, Method> methodMap;

    private RemoteCallServiceWrapper(final Object serviceImpl) {
        _service = serviceImpl;
        methods = _service.getClass().getMethods();
        methodMap = new HashMap<String, Method>();

        // Create a map for faster lookup
        for (final Method m : methods) {

            methodMap.put(Utils.createMethodFingerPrint(m), m);

        }

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
    public Method getMethod(final String method) {

        final Method m = methodMap.get(method);
    

        return m;

    }
}
