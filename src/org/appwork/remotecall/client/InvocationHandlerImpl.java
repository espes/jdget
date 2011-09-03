/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.client
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.appwork.remotecall.Utils;
import org.appwork.remotecall.server.ExceptionWrapper;
import org.appwork.remotecall.server.ServerInvokationException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;

/**
 * @author thomas
 * 
 */
public class InvocationHandlerImpl implements InvocationHandler {

    private final RemoteCallClient        client;
    private final HashMap<Method, String> methodMap;
    private final String                  name;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    /**
     * @param client
     * @param class1
     */
    public InvocationHandlerImpl(final RemoteCallClient client, final Class<?> class1) {
        this.client = client;
        this.name = class1.getSimpleName();
        this.methodMap = new HashMap<Method, String>();

        for (final Method m : class1.getMethods()) {
            this.methodMap.put(m, Utils.createMethodFingerPrint(m));

        }
    }

    public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        String returnValue;
        Object obj;

        try {
            returnValue = this.client.call(this.name, this.methodMap.get(method), args);
            obj = JSonStorage.restoreFromString(returnValue, new TypeRef(method.getGenericReturnType()) {
            }, null);
            return Utils.convert(obj, method.getReturnType());

        } catch (final ServerInvokationException e) {

            final ExceptionWrapper exception = JSonStorage.restoreFromString(e.getMessage(), ExceptionWrapper.class);
            final Throwable ex = exception.deserialiseException();
            // search to add the local cause
            final StackTraceElement[] localStack = new Exception().getStackTrace();
            final StackTraceElement[] newStack = new StackTraceElement[ex.getStackTrace().length + localStack.length - 1];
            System.arraycopy(ex.getStackTrace(), 0, newStack, 0, ex.getStackTrace().length);
            newStack[ex.getStackTrace().length] = new StackTraceElement("RemoteCallClient via", e.getRemoteID(), null, 0);
            System.arraycopy(localStack, 2, newStack, ex.getStackTrace().length + 1, localStack.length - 2);
            ex.setStackTrace(newStack);

            throw ex;
        }

    }

}
