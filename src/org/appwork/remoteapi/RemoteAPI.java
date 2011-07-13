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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.Regex;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.reflection.Clazz;

/**
 * @author daniel
 * 
 */
public class RemoteAPI implements HttpRequestHandler {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object v, final Class<T> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                v = ((Boolean) v).booleanValue();
            } else if (type == char.class) {
                v = (char) ((Long) v).byteValue();
            } else if (type == byte.class) {
                v = ((Number) v).byteValue();
            } else if (type == short.class) {
                v = ((Number) v).shortValue();
            } else if (type == int.class) {
                v = ((Number) v).intValue();
            } else if (type == long.class) {
                v = ((Number) v).longValue();
            } else if (type == float.class) {
                v = ((Number) v).floatValue();
            } else if (type == double.class) {
                //
                v = ((Number) v).doubleValue();

            }
        }

        return (T) v;
    }

    /* hashmap that holds all registered interfaces and their pathes */
    private final HashMap<String, InterfaceHandler<?>> interfaces = new HashMap<String, InterfaceHandler<?>>();

    private final Object                               LOCK       = new Object();

    public RemoteAPI() {
    }

    protected void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws UnsupportedEncodingException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ApiCommandNotAvailable, BadParameterException {

        final Method method = request.getMethod();
        if (method == null) { throw new ApiCommandNotAvailable(); }
        final Object[] parameters = new Object[method.getParameterTypes().length];
        boolean responseIsParameter = false;
        int count = 0;
        for (int i = 0; i < parameters.length; i++) {
            if (RemoteAPIRequest.class.isAssignableFrom(method.getParameterTypes()[i])) {
                parameters[i] = request;
            } else if (RemoteAPIResponse.class.isAssignableFrom(method.getParameterTypes()[i])) {
                responseIsParameter = true;
                parameters[i] = response;
            } else {
                try {
                    parameters[i] = this.convert(request.getParameters()[count], method.getGenericParameterTypes()[i]);
                } catch (final Throwable e) {
                    throw new BadParameterException(request.getParameters()[count], e);
                }
                count++;
            }
        }
        Object ret = null;
        try {
            ret = request.getIface().invoke(method, parameters);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RemoteAPIException) {
                final RemoteAPIException ex = (RemoteAPIException) cause;
                response.setResponseCode(ex.getResponseCode());
                String message = "";
                if (ex.getMessage() != null) {
                    message = ex.getMessage();
                }
                if (message.length() == 0) {
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "0"));
                } else {
                    final int length = message.getBytes("UTF-8").length;
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));
                    response.getOutputStream().write(message.getBytes("UTF-8"));
                }
                return;
            } else {
                throw e;
            }
        }
        if (responseIsParameter) { return; }
        if (Clazz.isVoid(method.getReturnType())) {
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "0"));
        } else {
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            String text = JSonStorage.toString(ret);
            if (request.getJqueryCallback() != null) {
                /* wrap response into a valid jquery response format */
                final StringBuilder sb = new StringBuilder();
                sb.append(request.getJqueryCallback());
                sb.append("(");
                sb.append(text);
                sb.append(");");
                text = sb.toString();
            }
            final int length = text.getBytes("UTF-8").length;
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));
            response.getOutputStream().write(text.getBytes("UTF-8"));
        }
    }

    /**
     * @param string
     * @param type
     * @return
     */
    private Object convert(String string, final Type type) {
        if (type == String.class && !string.startsWith("\"")) {
            string = "\"" + string + "\"";
        }
        @SuppressWarnings("unchecked")
        final Object v = JSonStorage.restoreFromString(string, new TypeRef(type) {
        }, null);
        if (type instanceof Class && Clazz.isPrimitive((Class<?>) type)) {
            return RemoteAPI.cast(v, (Class<?>) type);
        } else {
            return v;
        }
    }

    public RemoteAPIRequest getInterfaceHandler(final HttpRequest request) {
        final String[] intf = new Regex(request.getRequestedPath(), "/((.+)/)?(.+)$").getRow(0);
        if (intf == null || intf.length != 3) { return null; }
        InterfaceHandler<?> interfaceHandler = null;
        if (intf[1] == null) {
            intf[1] = "";
        }
        synchronized (this.LOCK) {
            interfaceHandler = this.interfaces.get(intf[1]);
        }
        if (interfaceHandler == null) { return null; }
        final ArrayList<String> parameters = new ArrayList<String>();
        String jqueryCallback = null;
        /* convert GET parameters to methodParameters */
        for (final String[] param : request.getRequestedURLParameters()) {
            if (param[1] != null) {
                /* key=value(parameter) */
                if ("callback".equalsIgnoreCase(param[0])) {
                    /* filter jquery callback */
                    jqueryCallback = param[1];
                    continue;
                }
                parameters.add(param[1]);
            } else {
                /* key(parameter) */
                parameters.add(param[0]);
            }
        }
        if (request instanceof PostRequest) {
            try {
                final LinkedList<String[]> ret = ((PostRequest) request).getPostParameter();
                if (ret != null) {
                    /* add POST parameters to methodParameters */
                    for (final String[] param : ret) {
                        if (param[1] != null) {
                            if ("callback".equalsIgnoreCase(param[0])) {
                                /* filter jquery callback */
                                jqueryCallback = param[1];
                                continue;
                            }
                            /* key=value(parameter) */
                            parameters.add(param[1]);
                        } else {
                            /* key(parameter) */
                            parameters.add(param[0]);
                        }
                    }
                }
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
        if (jqueryCallback != null) {
            // System.out.println("found jquery callback: " + jqueryCallback);
        }
        return new RemoteAPIRequest(interfaceHandler, intf[2], parameters.toArray(new String[] {}), request, jqueryCallback);
    }

    /**
     * @param interfaceHandler
     * @param string
     * @return
     */

    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public void register(final RemoteAPIInterface x) throws ParseException {
        synchronized (this.LOCK) {
            Class<?> clazz = x.getClass();
            while (clazz != null) {
                for (final Class<?> c : clazz.getInterfaces()) {
                    if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                        String namespace = c.getName();
                        final ApiNamespace a = c.getAnnotation(ApiNamespace.class);
                        if (a != null) {
                            namespace = a.value();
                        }
                        if (this.interfaces.containsKey(namespace)) { throw new IllegalStateException("Interface " + c.getName() + " with namespace " + namespace + " already has been registered by " + this.interfaces.get(namespace)); }
                        System.out.println("Register: " + c.getName() + "->" + namespace);
                        try {
                            this.interfaces.put(namespace, InterfaceHandler.create((Class<RemoteAPIInterface>) c, x));
                        } catch (final SecurityException e) {
                            throw new ParseException(e);
                        } catch (final NoSuchMethodException e) {
                            throw new ParseException(e);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    public void unregister(final RemoteAPIInterface x) {
        synchronized (this.LOCK) {
            Class<?> clazz = x.getClass();
            while (clazz != null) {
                for (final Class<?> c : clazz.getInterfaces()) {
                    if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                        String namespace = c.getName();
                        final ApiNamespace a = c.getAnnotation(ApiNamespace.class);
                        if (a != null) {
                            namespace = a.value();
                        }
                        this.interfaces.remove(namespace);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

}
