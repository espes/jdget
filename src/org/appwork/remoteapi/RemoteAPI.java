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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.remoteapi.annotations.AllowResponseAccess;
import org.appwork.remoteapi.annotations.ApiAuthLevel;
import org.appwork.remoteapi.annotations.ApiNamespace;
import org.appwork.remoteapi.annotations.ApiSessionRequired;
import org.appwork.remoteapi.exceptions.ApiCommandNotAvailable;
import org.appwork.remoteapi.exceptions.AuthException;
import org.appwork.remoteapi.exceptions.BadParameterException;
import org.appwork.remoteapi.exceptions.BasicRemoteAPIException;
import org.appwork.remoteapi.exceptions.InternalApiException;
import org.appwork.remoteapi.responsewrapper.DataObject;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.Regex;
import org.appwork.utils.ReusableByteArrayOutputStream;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.net.ChunkedOutputStream;
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

    /**
     * @param string
     * @param type
     * @return
     */
    private static Object convert(String string, final Type type) {
        if ((type == String.class || type instanceof Class && ((Class<?>) type).isEnum()) && !string.startsWith("\"")) {
            /* workaround if strings are not escaped, same for enums */
            if ("null".equals(string)) { return null; }
            string = "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Object v = JSonStorage.restoreFromString(string, new TypeRef(type) {
        }, null);
        if (type instanceof Class && Clazz.isPrimitive(type)) {
            return RemoteAPI.cast(v, (Class<?>) type);
        } else {
            return v;
        }
    }

    public static OutputStream getOutputStream(final RemoteAPIResponse response, final RemoteAPIRequest request, final boolean gzip, final boolean wrapJQuery) throws IOException {
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CACHE_CONTROL, "no-store, no-cache"));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/json"));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
        if (gzip) {
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
        }
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        final OutputStream os = response.getOutputStream(true);
        final ChunkedOutputStream cos = new ChunkedOutputStream(os);
        final OutputStream uos;
        final GZIPOutputStream out;
        if (gzip) {
            uos = out = new GZIPOutputStream(cos);
        } else {
            out = null;
            uos = cos;
        }
        return new OutputStream() {
            boolean wrapperHeader = wrapJQuery && request != null && request.getJqueryCallback() != null;
            boolean wrapperEnd    = wrapJQuery && request != null && request.getJqueryCallback() != null;

            @Override
            public void close() throws IOException {
                this.wrapperEnd();
                if (out != null) {
                    out.finish();
                    out.flush();
                }
                uos.close();
            }

            @Override
            public void flush() throws IOException {
                uos.flush();
            }

            private void wrapperEnd() throws UnsupportedEncodingException, IOException {
                if (this.wrapperEnd) {
                    uos.write(")".getBytes("UTF-8"));
                    this.wrapperEnd = false;
                }
            }

            private void wrapperHeader() throws UnsupportedEncodingException, IOException {
                if (this.wrapperHeader) {
                    uos.write(request.getJqueryCallback().getBytes("UTF-8"));
                    uos.write("(".getBytes("UTF-8"));
                    this.wrapperHeader = false;
                }
            }

            @Override
            public void write(final byte[] b) throws IOException {
                this.wrapperHeader();
                uos.write(b);
            }

            @Override
            public void write(final int b) throws IOException {
                this.wrapperHeader();
                uos.write(b);
            }
        };
    }

    public static boolean gzip(final RemoteAPIRequest request) {
        final HTTPHeader acceptEncoding = request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_ACCEPT_ENCODING);
        if (acceptEncoding != null) {
            final String value = acceptEncoding.getValue();
            if (value != null && value.contains("gzip")) { return true; }
        }
        return false;
    }

    public static void sendBytes(final RemoteAPIResponse response, final boolean gzip, final boolean chunked, final byte[] bytes) throws IOException {
        /* we dont want this api response to get cached */
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CACHE_CONTROL, "no-store, no-cache"));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/json"));
        if (gzip == false) {
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
            response.getOutputStream(true).write(bytes);
        } else {
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
            if (chunked == false) {
                final ReusableByteArrayOutputStream ros = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(1024);
                try {
                    final GZIPOutputStream out = new GZIPOutputStream(ros);
                    out.write(bytes);
                    out.finish();
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, ros.size() + ""));
                    response.getOutputStream(true).write(ros.getInternalBuffer(), 0, ros.size());
                } finally {
                    try {
                        ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(ros);
                    } catch (final Throwable e) {
                    }
                }
            } else {
                response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
                ChunkedOutputStream cos = null;
                GZIPOutputStream out = null;
                try {
                    cos = new ChunkedOutputStream(response.getOutputStream(true));
                    out = new GZIPOutputStream(cos);
                    out.write(bytes);
                } finally {
                    try {
                        out.finish();
                    } catch (final Throwable e) {
                    }
                    try {
                        out.flush();
                    } catch (final Throwable e) {
                    }
                    try {
                        cos.sendEOF();
                    } catch (final Throwable e) {
                    }
                }
            }
        }

    }

    /* hashmap that holds all registered interfaces and their pathes */
    private final HashMap<String, InterfaceHandler<RemoteAPIInterface>> interfaces = new HashMap<String, InterfaceHandler<RemoteAPIInterface>>();

    private final Object                                                LOCK       = new Object();

    public RemoteAPI() {

    }

    @SuppressWarnings("unchecked")
    protected void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws BasicRemoteAPIException {
        Object responseData = null;

        final Method method = request.getMethod();
        try {
            if (method == null) { throw new ApiCommandNotAvailable(); }

            this.authenticate(method, request, response);

            final Object[] parameters = new Object[method.getParameterTypes().length];
            boolean methodHasReturnTypeAndAResponseParameter = false;
            boolean methodHasResponseParameter = false;
            int count = 0;
            for (int i = 0; i < parameters.length; i++) {
                if (RemoteAPIRequest.class.isAssignableFrom(method.getParameterTypes()[i])) {
                    parameters[i] = request;
                } else if (RemoteAPIResponse.class.isAssignableFrom(method.getParameterTypes()[i])) {
                    methodHasResponseParameter = true;
                    if (method.getAnnotation(AllowResponseAccess.class) != null) {
                        methodHasReturnTypeAndAResponseParameter = true;
                    }
                    parameters[i] = response;
                } else {
                    try {
                        parameters[i] = RemoteAPI.convert(request.getParameters()[count], method.getGenericParameterTypes()[i]);
                    } catch (final Throwable e) {
                        throw new BadParameterException(e, request.getParameters()[count]);
                    }
                    count++;
                }
            }
            try {
                responseData = request.getIface().invoke(method, parameters);
            } catch (final InvocationTargetException e) {
                throw e.getTargetException();
            }
            if (methodHasResponseParameter && !methodHasReturnTypeAndAResponseParameter) {
                /*
                 * TODO: check for unhandled response, be aware of async
                 * responses!
                 */
                return;
            }

            String text = null;
            responseData = this.handleVoidMethods(responseData, method);

            if (method.getAnnotation(ResponseWrapper.class) != null) {
                text = ((AbstractResponseWrapper<Object>) method.getAnnotation(ResponseWrapper.class).value().newInstance()).toString(responseData);
            } else {
                text = this.toString(request, response, responseData);
            }
            text = this.jQueryWrap(request, text);
            this.sendText(request, response, text);
        } catch (final BasicRemoteAPIException e) {
            // set request and response if it has not set yet
            if (e.getRequest() == null) {
                e.setRequest(request);
            }
            if (e.getResponse() == null) {
                e.setResponse(response);
            }
            throw e;
        } catch (final Throwable e) {
            e.printStackTrace();
            final InternalApiException internal = new InternalApiException(e);
            internal.setRequest(request);
            internal.setResponse(response);

            throw internal;
        }

    }

    /**
     * @param method
     * @param request
     * @param response
     * @throws BasicRemoteAPIException
     */
    protected void authenticate(final Method method, final RemoteAPIRequest request, final RemoteAPIResponse response) throws BasicRemoteAPIException {
        if (request.getIface().getSignatureHandler() != null && request.getIface().isSignatureRequired(method)) {
            /* maybe this request is handled by rawMethodHandler */
            final Object[] parameters = new Object[] { request, response };
            Object responseData;
            try {
                try {
                    responseData = request.getIface().invoke(request.getIface().getSignatureHandler(), parameters);

                    if (!Boolean.TRUE.equals(responseData)) { throw new AuthException(); }
                } catch (final InvocationTargetException e) {
                    throw e.getTargetException();
                }
            } catch (final BasicRemoteAPIException e) {
                throw e;
            } catch (final Throwable e) {
                throw new InternalApiException(e);
            }
        }
    }

    public RemoteAPIRequest getInterfaceHandler(final HttpRequest request) {
        final String[] intf = new Regex(request.getRequestedPath(), "/((.+)/)?(.+)$").getRow(0);
        if (intf == null || intf.length != 3) { return null; }
        /* intf=unimportant,namespace,method */
        if (intf[2] != null && intf[2].endsWith("/")) {
            /* special handling for commands without name */
            /**
             * Explanation: this is for special handling of this
             * http://localhost/test -->this is method test in root
             * http://localhost/test/ --> this is method without name in
             * namespace test
             */
            intf[1] = intf[2].substring(0, intf[2].length() - 1);
            intf[2] = "";
        }
        InterfaceHandler<?> interfaceHandler = null;
        if (intf[1] == null) {
            intf[1] = "";
        }
        synchronized (this.LOCK) {
            interfaceHandler = this.interfaces.get(intf[1]);
        }
        if (interfaceHandler == null) { return null; }
        final java.util.List<String> parameters = new ArrayList<String>();
        String jqueryCallback = null;
        String signature = null;
        Long requestID = null;
        /* convert GET parameters to methodParameters */
        for (final String[] param : request.getRequestedURLParameters()) {
            if (param[1] != null) {
                /* key=value(parameter) */
                if ("callback".equalsIgnoreCase(param[0])) {
                    /* filter jquery callback */
                    jqueryCallback = param[1];
                    continue;
                } else if ("signature".equalsIgnoreCase(param[0])) {
                    /* filter url signature */
                    signature = param[1];
                    continue;
                } else if ("rid".equalsIgnoreCase(param[0])) {
                    requestID = Long.parseLong(param[1]);
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
        return new RemoteAPIRequest(interfaceHandler, intf[2], parameters.toArray(new String[] {}), request, jqueryCallback, signature, requestID);
    }

    /**
     * @param responseData
     * @param method
     * @return
     */
    protected Object handleVoidMethods(Object responseData, final Method method) {
        if (Clazz.isVoid(method.getReturnType())) {
            // void return
            responseData = "";
        }
        return responseData;
    }

    /**
     * override this if you want to authorize usage of methods
     * 
     * @param request
     * @return
     */
    protected boolean isAllowed(final RemoteAPIRequest request, final RemoteAPIResponse response) {
        return true;
    }

    /**
     * @param request
     * @param text
     * @return
     */
    protected String jQueryWrap(final RemoteAPIRequest request, String text) {
        if (request.getJqueryCallback() != null) {
            /* wrap response into a valid jquery callback response format */
            final StringBuilder sb = new StringBuilder();
            sb.append(request.getJqueryCallback());
            sb.append("(");
            sb.append(text);
            sb.append(");");
            text = sb.toString();
        }
        return text;
    }

    /**
     * @param interfaceHandler
     * @param string
     * @return
     */
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return this.onUnknownRequest(request, response); }
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        final RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return this.onUnknownRequest(request, response); }
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected boolean onUnknownRequest(final HttpRequest request, final HttpResponse response) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public void register(final RemoteAPIInterface x) throws ParseException {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        synchronized (this.LOCK) {
            Class<?> clazz = x.getClass();
            while (clazz != null) {
                main: for (final Class<?> c : clazz.getInterfaces()) {
                    if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                        for (final Class<?> e : interfaces) {
                            /* avoid multiple adding of same interfaces */
                            if (c.isAssignableFrom(e)) {
                                continue main;
                            }
                        }
                        interfaces.add(c);
                        String namespace = c.getName();
                        final ApiNamespace a = c.getAnnotation(ApiNamespace.class);
                        if (a != null) {
                            namespace = a.value();
                        }
                        int defaultAuthLevel = 0;
                        final ApiAuthLevel b = c.getAnnotation(ApiAuthLevel.class);
                        if (b != null) {
                            defaultAuthLevel = b.value();
                        }
                        // if (this.interfaces.containsKey(namespace)) { throw
                        // new IllegalStateException("Interface " + c.getName()
                        // + " with namespace " + namespace +
                        // " already has been registered by " +
                        // this.interfaces.get(namespace)); }
                        // System.out.println("Register:   " + c.getName() +
                        // "->" + namespace);
                        // try {

                        System.out.println("Register:   " + c.getName() + "->" + namespace);
                        try {
                            InterfaceHandler<RemoteAPIInterface> handler = this.interfaces.get(namespace);
                            if (handler == null) {
                                handler = InterfaceHandler.create((Class<RemoteAPIInterface>) c, x, defaultAuthLevel);
                                handler.setSessionRequired(c.getAnnotation(ApiSessionRequired.class) != null);
                                this.interfaces.put(namespace, handler);
                            } else {
                                handler.add((Class<RemoteAPIInterface>) c, x, defaultAuthLevel);
                            }

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

    /**
     * @param request
     * @param response
     * @param text
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void sendText(final RemoteAPIRequest request, final RemoteAPIResponse response, final String text) throws UnsupportedEncodingException, IOException {
        final byte[] bytes = text.getBytes("UTF-8");
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        RemoteAPI.sendBytes(response, RemoteAPI.gzip(request), true, bytes);
    }

    /**
     * @param responseData
     * @param responseData2
     * @return
     */
    protected String toString(final RemoteAPIRequest request, final RemoteAPIResponse response, final Object responseData) {

        return JSonStorage.toString(new DataObject(responseData));
    }

    public void unregister(final RemoteAPIInterface x) {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        synchronized (this.LOCK) {
            Class<?> clazz = x.getClass();
            while (clazz != null) {
                main: for (final Class<?> c : clazz.getInterfaces()) {
                    if (RemoteAPIInterface.class.isAssignableFrom(c)) {
                        for (final Class<?> e : interfaces) {
                            /* avoid multiple removing of same interfaces */
                            if (c.isAssignableFrom(e)) {
                                continue main;
                            }
                        }
                        interfaces.add(c);
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
