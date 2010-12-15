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
import java.net.URLDecoder;
import java.util.HashMap;

import org.appwork.remotecall.Utils;
import org.appwork.storage.JSonStorage;

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
        name = class1.getSimpleName();
        methodMap = new HashMap<Method, String>();

        for (final Method m : class1.getMethods()) {
            methodMap.put(m, Utils.createMethodFingerPrint(m));

        }
    }

    public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final String returnValue = client.call(name, methodMap.get(method), args);

        final Object obj = JSonStorage.restoreFromString(URLDecoder.decode(returnValue, "UTF-8"), method.getReturnType());
        return Utils.convert(obj, method.getReturnType());

    }

}
