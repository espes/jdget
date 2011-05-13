/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.HTTPHeader;

/**
 * @author thomas
 * 
 */
public class InterfaceHandler<T> {

    /**
     * @param c
     * @param x
     * @return
     * @throws ParseException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static <T extends RemoteAPIInterface> InterfaceHandler<T> create(final Class<T> c, final RemoteAPIInterface x) throws ParseException, SecurityException, NoSuchMethodException {
        final InterfaceHandler<T> ret = new InterfaceHandler<T>(c, x);
        ret.parse();
        return ret;
    }

    private final RemoteAPIInterface                        impl;
    private final Class<T>                                  interfaceClass;
    private final TreeMap<String, TreeMap<Integer, Method>> methods;
    private final HashMap<Method, Integer>                  parameterCountMap;
    private static Method                                   HELP;
    static {
        try {
            InterfaceHandler.HELP = InterfaceHandler.class.getMethod("help", new Class[] { RemoteAPIRequest.class, RemoteAPIResponse.class });
        } catch (final SecurityException e) {
            Log.exception(e);
        } catch (final NoSuchMethodException e) {
            Log.exception(e);
        }

    }

    /**
     * @param <T>
     * @param c
     * @param x
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private InterfaceHandler(final Class<T> c, final RemoteAPIInterface x) throws SecurityException, NoSuchMethodException {
        this.interfaceClass = c;
        this.impl = x;
        this.methods = new TreeMap<String, TreeMap<Integer, Method>>();
        TreeMap<Integer, Method> map;
        this.methods.put("help", map = new TreeMap<Integer, Method>());
        map.put(0, InterfaceHandler.HELP);
        this.parameterCountMap = new HashMap<Method, Integer>();
        this.parameterCountMap.put(InterfaceHandler.HELP, 0);
    }

    /**
     * @param length
     * @param methodName
     * @return
     */
    public Method getMethod(final String methodName, final int length) {
        if (methodName.equals(InterfaceHandler.HELP.getName())) { return InterfaceHandler.HELP; }

        final TreeMap<Integer, Method> methodsByName = this.methods.get(methodName);
        if (methodsByName == null) { return null; }

        return methodsByName.get(length);
    }

    /**
     * @param method
     * @return
     */
    public int getParameterCount(final Method method) {

        return this.parameterCountMap.get(method);
    }

    public void help(final RemoteAPIRequest request, final RemoteAPIResponse response) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException, IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.interfaceClass.getName());
        sb.append("\r\n\r\n");
        Entry<String, TreeMap<Integer, Method>> next;
        for (final Iterator<Entry<String, TreeMap<Integer, Method>>> it = this.methods.entrySet().iterator(); it.hasNext();) {
            next = it.next();
            for (final Method m : next.getValue().values()) {
                if (m == InterfaceHandler.HELP) {
                    sb.append("\r\n====- " + m.getName() + " -====");
                    sb.append("\r\n    Description: This Call");
                    sb.append("\r\n           Call: ");
                    sb.append("/" + m.getName() + "\r\n");
                    continue;

                }
                sb.append("\r\n====- " + m.getName() + " -====");
                final ApiDoc an = m.getAnnotation(ApiDoc.class);
                if (an != null) {

                    sb.append("\r\n    Description: ");
                    sb.append(an.value() + "");
                }
                // sb.append("\r\n    Description: ");

                final HashMap<Type, Integer> map = new HashMap<Type, Integer>();
                String call = "/" + m.getName();
                int count = 0;
                for (int i = 0; i < m.getGenericParameterTypes().length; i++) {
                    if (m.getParameterTypes()[i] == RemoteAPIRequest.class || m.getParameterTypes()[i] == RemoteAPIResponse.class) {
                        continue;
                    }
                    count++;
                    if (i > 0) {
                        call += "&";

                    } else {
                        call += "?";
                    }

                    Integer num = map.get(m.getParameterTypes()[i]);
                    if (num == null) {
                        map.put(m.getParameterTypes()[i], 0);
                        num = 0;
                    }
                    num++;
                    call += m.getParameterTypes()[i].getSimpleName() + "" + num;
                    sb.append("\r\n      Parameter: " + count + " - " + m.getParameterTypes()[i].getSimpleName() + "" + num);
                    map.put(m.getParameterTypes()[i], num);

                }
                sb.append("\r\n           Call: " + call);

                sb.append("\r\n");
            }
        }
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        final String text = sb.toString();

        final int length = text.getBytes().length;
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));
        response.getOutputStream().write(text.getBytes("UTF-8"));
    }

    /**
     * @param method
     * @param parameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public Object invoke(final Method method, final Object[] parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (method.getDeclaringClass() == InterfaceHandler.class) {
            return method.invoke(this, parameters);
        } else {
            return method.invoke(this.impl, parameters);
        }
    }

    /**
     * @throws ParseException
     * 
     */
    private void parse() throws ParseException {
        for (final Method m : this.interfaceClass.getMethods()) {
            this.validateMethod(m);
            TreeMap<Integer, Method> methodsByName = this.methods.get(m.getName());
            if (methodsByName == null) {
                methodsByName = new TreeMap<Integer, Method>();
                this.methods.put(m.getName(), methodsByName);
            }
            int l = 0;
            for (final Class<?> c : m.getParameterTypes()) {
                if (c != RemoteAPIRequest.class && c != RemoteAPIResponse.class) {
                    l++;
                }
            }
            this.parameterCountMap.put(m, l);
            if (methodsByName.containsKey(l)) { throw new ParseException(this.interfaceClass + " Contains ambiguous methods: \r\n" + m + "\r\n" + methodsByName.get(l)); }
            methodsByName.put(l, m);

        }

    }

    /**
     * @param m
     * @throws ParseException
     */
    private void validateMethod(final Method m) throws ParseException {
        if (m == InterfaceHandler.HELP) { throw new ParseException(m + " is reserved for internal usage"); }
        boolean responseIsParamater = false;
        for (final Type t : m.getGenericParameterTypes()) {
            if (RemoteAPIRequest.class == t) {
                continue;
            } else if (RemoteAPIResponse.class == t) {
                responseIsParamater = true;
                continue;
            } else {

                try {
                    JSonStorage.canStore(t);
                } catch (final InvalidTypeException e) {
                    throw new ParseException("Parameter " + t + " of " + m + " is invalid", e);
                }
            }
        }
        if (responseIsParamater) {
            if (m.getGenericReturnType() != void.class && m.getGenericReturnType() != Void.class) { throw new ParseException("Response in Parameters. " + m + " must return void, and has to handle the response itself"); }
        } else {
            try {
                JSonStorage.canStore(m.getGenericReturnType());
            } catch (final InvalidTypeException e) {
                throw new ParseException("return Type of " + m + " is invalid", e);
            }
        }

    }

}
