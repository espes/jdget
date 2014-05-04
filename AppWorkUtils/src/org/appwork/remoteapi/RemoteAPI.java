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
import java.util.List;
import java.util.regex.Pattern;
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
import org.appwork.utils.net.ChunkedOutputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.KeyValuePair;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.reflection.Clazz;

/**
 * @author daniel
 * 
 */
public class RemoteAPI implements HttpRequestHandler {
    public static class RemoteAPIMethod {

        private final InterfaceHandler<?> interfaceHandler;

        private final String              methodName;

        private final String              nameSpace;

        public RemoteAPIMethod(final String nameSpace, final InterfaceHandler<?> interfaceHandler, final String methodName) {
            this.nameSpace = nameSpace;
            this.interfaceHandler = interfaceHandler;
            this.methodName = methodName;
        }

        public final InterfaceHandler<?> getInterfaceHandler() {
            return this.interfaceHandler;
        }

        public final String getMethodName() {
            return this.methodName;
        }

        public final String getNameSpace() {
            return this.nameSpace;
        }
    }

    final static Pattern INTF = Pattern.compile("/((.+)/)?(.+)$");

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
    public static Object convert(String string, final Type type) {
        if ((type == String.class || type instanceof Class && ((Class<?>) type).isEnum()) && !string.startsWith("\"")) {
            /* workaround if strings are not escaped, same for enums */
            if ("null".equals(string)) { return null; }

            // string = "\"" + string.replace("\\", "\\\\").replace("\"",
            // "\\\"") + "\"";
            string = JSonStorage.serializeToJson(string);
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Object v = JSonStorage.restoreFromString(string, new TypeRef(type) {
        });
        if (type instanceof Class && Clazz.isPrimitive(type)) {
            return RemoteAPI.cast(v, (Class<?>) type);
        } else {
            return v;
        }
    }

    public static boolean deflate(final RemoteAPIRequest request) {
        final HTTPHeader acceptEncoding = request.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_ACCEPT_ENCODING);
        if (acceptEncoding != null) {
            final String value = acceptEncoding.getValue();
            if (value != null && value.contains("deflate")) { return true; }
        }
        return false;
    }

    @Deprecated
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

    /* hashmap that holds all registered interfaces and their pathes */
    private HashMap<String, InterfaceHandler<RemoteAPIInterface>> interfaces = new HashMap<String, InterfaceHandler<RemoteAPIInterface>>();

    public RemoteAPI() {

    }

    @SuppressWarnings("unchecked")
    protected void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws BasicRemoteAPIException {
        Object responseData = null;

        final Method method = request.getMethod();
        try {
            if (method == null) {
                //
                throw new ApiCommandNotAvailable(request.getRequestedURL());
            }

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
            this.writeStringResponse(responseData, method, request, response);
        } catch (BasicRemoteAPIException e) {
            // set request and response if it has not set yet
            if (e.getRequest() == null) {
                e.setRequest(request);
            }
            if (e.getResponse() == null) {
                e.setResponse(response);
            }
            e = this.preProcessBasicRemoteAPIException(request, response, e);
            if (e != null) { throw e; }
        } catch (final Throwable e) {
            e.printStackTrace();
            final InternalApiException internal = new InternalApiException(e);
            internal.setRequest(request);
            internal.setResponse(response);
            final BasicRemoteAPIException ret = this.preProcessBasicRemoteAPIException(request, response, internal);
            if (ret != null) { throw ret; }
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

    public RemoteAPIRequest createRemoteAPIRequestObject(final HttpRequest request) throws BasicRemoteAPIException {
        this.validateRequest(request);
        final RemoteAPIMethod remoteAPIMethod = this.getRemoteAPIMethod(request);
        if (remoteAPIMethod == null) { return null; }
        final java.util.List<String> parameters = new ArrayList<String>();
        String jqueryCallback = null;
        /* convert GET parameters to methodParameters */
        for (final KeyValuePair param : request.getRequestedURLParameters()) {
            if (param.key != null) {
                /* key=value(parameter) */
                if ("callback".equalsIgnoreCase(param.key)) {
                    /* filter jquery callback */
                    jqueryCallback = param.value;
                    continue;
                } else if ("signature".equalsIgnoreCase(param.key)) {
                    /* filter url signature */
                    continue;
                } else if ("rid".equalsIgnoreCase(param.key)) {
                    continue;
                }
            }
            parameters.add(param.value);
        }
        if (request instanceof PostRequest) {
            try {
                final List<KeyValuePair> ret = ((PostRequest) request).getPostParameter();
                if (ret != null) {
                    /* add POST parameters to methodParameters */
                    for (final KeyValuePair param : ret) {
                        if (param.key != null) {
                            /* key=value(parameter) */
                            if ("callback".equalsIgnoreCase(param.key)) {
                                /* filter jquery callback */
                                jqueryCallback = param.value;
                                continue;
                            }
                        }
                        /* key(parameter) */
                        parameters.add(param.value);
                    }
                }
            } catch (final Throwable e) {
                if (e instanceof BasicRemoteAPIException) { throw (BasicRemoteAPIException) e; }
                if (e.getCause() instanceof BasicRemoteAPIException) { throw (BasicRemoteAPIException) e.getCause(); }
                throw new RuntimeException(e);
            }
        }
        // if (jqueryCallback != null) {
        // System.out.println("found jquery callback: " + jqueryCallback);
        // }
        RemoteAPIRequest ret;
        try {
            ret = this.createRemoteAPIRequestObject(request, remoteAPIMethod.getMethodName(), remoteAPIMethod.getInterfaceHandler(), parameters, jqueryCallback);
        } catch (final IOException e) {
            throw new BasicRemoteAPIException(e);
        }
        this.validateRequest(ret);
        return ret;
    }

    protected RemoteAPIRequest createRemoteAPIRequestObject(final HttpRequest request, final String method, final InterfaceHandler<?> interfaceHandler, final java.util.List<String> parameters, final String jqueryCallback) throws IOException {
        return new RemoteAPIRequest(interfaceHandler, method, parameters.toArray(new String[] {}), request, jqueryCallback);
    }

    protected RemoteAPIResponse createRemoteAPIResponseObject(final RemoteAPIRequest request, final HttpResponse response) throws IOException {
        return new RemoteAPIResponse(response, this);
    }

    public RemoteAPIMethod getRemoteAPIMethod(final HttpRequest request) throws BasicRemoteAPIException {
        final String path = request.getRequestedPath();
        final String[] intf = new Regex(path, RemoteAPI.INTF).getRow(0);
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
        if (intf[1] == null) {
            intf[1] = "";
        }
        final InterfaceHandler<RemoteAPIInterface> ret = this.interfaces.get(intf[1]);
        if (ret != null) { return new RemoteAPIMethod(intf[1], ret, intf[2]); }
        return null;
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
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        final RemoteAPIRequest apiRequest = this.createRemoteAPIRequestObject(request);
        if (apiRequest == null) { return this.onUnknownRequest(request, response); }
        try {
            this._handleRemoteAPICall(apiRequest, this.createRemoteAPIResponseObject(apiRequest, response));
        } catch (final IOException e) {
            throw new BasicRemoteAPIException(e);
        }
        return true;
    }

    public boolean onPostRequest(final PostRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        final RemoteAPIRequest apiRequest = this.createRemoteAPIRequestObject(request);
        if (apiRequest == null) { return this.onUnknownRequest(request, response); }
        try {
            this._handleRemoteAPICall(apiRequest, this.createRemoteAPIResponseObject(apiRequest, response));
        } catch (final IOException e) {
            throw new BasicRemoteAPIException(e);
        }
        return true;
    }

    protected boolean onUnknownRequest(final HttpRequest request, final HttpResponse response) {
        return false;
    }

    protected BasicRemoteAPIException preProcessBasicRemoteAPIException(final RemoteAPIRequest request, final RemoteAPIResponse response, final BasicRemoteAPIException e) {
        return e;
    }

    @SuppressWarnings("unchecked")
    public void register(final RemoteAPIInterface x) throws ParseException {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        synchronized (this) {
            final HashMap<String, InterfaceHandler<RemoteAPIInterface>> linterfaces = new HashMap<String, InterfaceHandler<RemoteAPIInterface>>(this.interfaces);
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

                        // System.out.println("Register:   " + c.getName() +
                        // "->" + namespace);
                        try {
                            InterfaceHandler<RemoteAPIInterface> handler = linterfaces.get(namespace);
                            if (handler == null) {
                                handler = InterfaceHandler.create((Class<RemoteAPIInterface>) c, x, defaultAuthLevel);
                                handler.setSessionRequired(c.getAnnotation(ApiSessionRequired.class) != null);
                                linterfaces.put(namespace, handler);
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
                this.interfaces = linterfaces;
            }
        }
    }

    /**
     * @param request
     * @param response
     * @param text
     * @param chunked
     *            TODO
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void sendText(final RemoteAPIRequest request, final RemoteAPIResponse response, String text) throws UnsupportedEncodingException, IOException {
        text = this.jQueryWrap(request, text);
        final byte[] bytes = text.getBytes("UTF-8");
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        response.sendBytes(request, bytes);
    }

    /**
     * @param responseData
     * @param responseData2
     * @return
     */
    public String toString(final RemoteAPIRequest request, final RemoteAPIResponse response, final Object responseData) {
        return JSonStorage.serializeToJson(new DataObject(responseData));
    }

    public void unregister(final RemoteAPIInterface x) {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        synchronized (this) {
            final HashMap<String, InterfaceHandler<RemoteAPIInterface>> linterfaces = new HashMap<String, InterfaceHandler<RemoteAPIInterface>>(this.interfaces);
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
                        linterfaces.remove(namespace);
                    }
                }
                clazz = clazz.getSuperclass();
            }
            this.interfaces = linterfaces;
        }
    }

    /**
     * @param request
     */
    protected void validateRequest(final HttpRequest request) throws BasicRemoteAPIException {
        // TODO Auto-generated method stub
    }

    /**
     * @param ret
     */
    protected void validateRequest(final RemoteAPIRequest ret) throws BasicRemoteAPIException {
        // TODO Auto-generated method stub

    }

    public void writeStringResponse(Object responseData, final Method method, final RemoteAPIRequest request, final RemoteAPIResponse response) throws BasicRemoteAPIException {
        try {
            String text = null;
            if (method != null) {
                responseData = this.handleVoidMethods(responseData, method);
            }
            if (method != null && method.getAnnotation(ResponseWrapper.class) != null) {
                text = ((AbstractResponseWrapper<Object>) method.getAnnotation(ResponseWrapper.class).value().newInstance()).toString(responseData);
            } else {
                text = this.toString(request, response, responseData);
            }

            this.sendText(request, response, text);
        } catch (final Throwable e) {
            final InternalApiException internal = new InternalApiException(e);
            internal.setRequest(request);
            internal.setResponse(response);
            throw internal;
        }
    }
}
