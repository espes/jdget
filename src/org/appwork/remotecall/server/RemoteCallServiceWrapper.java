package org.appwork.remotecall.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.appwork.remotecall.RemoteCallInterface;
import org.appwork.remotecall.Utils;

public class RemoteCallServiceWrapper {

    private final Object                  _service;
    private final Method[]                methods;
    private final HashMap<String, Method> methodMap;

    public <T extends RemoteCallInterface> RemoteCallServiceWrapper(Class<T> class1, T serviceImpl) throws ParsingException {
        _service = serviceImpl;
        methods = class1.getMethods();
        methodMap = new HashMap<String, Method>();

        // Create a map for faster lookup
        for (final Method m : methods) {

            methodMap.put(Utils.createMethodFingerPrint(m), m);
            for (Class<?> e : m.getExceptionTypes()) {
                if (e.isAssignableFrom(RemoteCallException.class)) { throw new ParsingException(m + " exceptions do not extend RemoteCallException"); }
                
                try {
                    e.getConstructor();
             
                } catch (Throwable e1) {
                    e1.printStackTrace();
                    throw new ParsingException(e + " no accessable null constructor available");
                }
            }

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
