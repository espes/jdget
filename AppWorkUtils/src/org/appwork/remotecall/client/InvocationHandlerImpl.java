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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;

import org.appwork.remotecall.RemoteCallInterface;
import org.appwork.remotecall.Utils;
import org.appwork.remotecall.server.ExceptionWrapper;
import org.appwork.remotecall.server.ParsingException;
import org.appwork.remotecall.server.RemoteCallException;
import org.appwork.remotecall.server.ServerInvokationException;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.InterfaceParseException;
import org.appwork.utils.logging.Log;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * 
 */
public class InvocationHandlerImpl<T extends RemoteCallInterface> implements InvocationHandler {

    private final RemoteCallClient         client;

    private final String                   name;
    private Class<T>                       interfaceClass;
    private HashMap<String, MethodHandler> handler;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    /**
     * @param client
     * @param class1
     * @throws ParsingException
     */
    public InvocationHandlerImpl(final RemoteCallClient client, final Class<T> class1) throws ParsingException {
        this.client = client;
        this.name = class1.getSimpleName();
        interfaceClass = class1;

        parse();

    }

    /**
     * 
     */
    private void parse() {

        Class<?> clazz = this.interfaceClass;
        this.handler = new HashMap<String, MethodHandler>();
        final HashSet<String> dupe = new HashSet<String>();
        while (clazz != null && clazz != RemoteCallInterface.class) {
            for (final Method m : clazz.getDeclaredMethods()) {

                if (!dupe.add(m.getName())) { throw new InterfaceParseException("Method " + m.getName() + " is avlailable twice in " + clazz); }
                try {
                    if (m.getGenericReturnType() != void.class) {
                        JSonStorage.canStore(m.getGenericReturnType(), false);
                    }
                    for (final Type t : m.getGenericParameterTypes()) {
                        JSonStorage.canStore(t, false);
                    }
                } catch (final InvalidTypeException e) {
                    Log.exception(e);
                    throw new InterfaceParseException("Json Serialize not possible for " + m);
                }
                for (final Class<?> e : m.getExceptionTypes()) {
                    if (!RemoteCallException.class.isAssignableFrom(e)) {
                        //

                        throw new InterfaceParseException(m + " exceptions do not extend RemoteCallException");
                    }
                    try {
                        e.getConstructors();

                    } catch (final Throwable e1) {
                        throw new InterfaceParseException(e + " no accessable null constructor available");
                    }
                }
                handler.put(m.getName(), new MethodHandler(m));
            }

            // run down the calss hirarchy to find all methods. getMethods does
            // not work, because it only finds public methods
            final Class<?>[] interfaces = clazz.getInterfaces();
            clazz = interfaces[0];

        }

    }

    public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        Object returnValue;
        Object obj;

        try {

            returnValue = this.client.call(this.name, method, args);

            if (Clazz.isVoid(method.getGenericReturnType())) {
                return null;
            } else if (returnValue instanceof byte[] && Clazz.isByteArray(method.getGenericReturnType())) {
                return returnValue;
            } else {
                final TypeRef<Object> tr = new TypeRef<Object>(method.getGenericReturnType()) {
                };

                obj = JSonStorage.restoreFromString((String) returnValue, tr, null);
                return Utils.convert(obj, tr.getType());
            }

        } catch (final ServerInvokationException e) {

            final ExceptionWrapper exception = JSonStorage.restoreFromString(e.getMessage(), ExceptionWrapper.class);
            final Throwable ex = exception.deserialiseException();
            // search to add the local cause
            final StackTraceElement[] localStack = new Exception().getStackTrace();
            final StackTraceElement[] newStack = new StackTraceElement[localStack.length - 1];
            System.arraycopy(localStack, 2, newStack, 0, localStack.length - 2);
            newStack[newStack.length - 1] = new StackTraceElement("RemoteCallClient via", e.getRemoteID() + "", null, 0);
            ex.setStackTrace(newStack);

            throw ex;
        }

    }

    /**
     * @return
     * 
     */
    public HashMap<String, MethodHandler> getHandler() {
        return handler;

    }
}
