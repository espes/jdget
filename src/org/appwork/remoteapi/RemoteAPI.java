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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.Regex;
import org.appwork.utils.ReusableByteArrayOutputStream;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.logging.Log;
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
public class RemoteAPI implements HttpRequestHandler, RemoteAPIProcessList {

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
        final OutputStream os = response.getOutputStream();
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

    protected static void sendBytes(final RemoteAPIResponse response, final boolean gzip, final boolean chunked, final byte[] bytes) throws IOException {
        /* we dont want this api response to get cached */
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CACHE_CONTROL, "no-store, no-cache"));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/json"));
        if (gzip == false) {
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
            response.getOutputStream().write(bytes);
        } else {
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
            if (chunked == false) {
                final ReusableByteArrayOutputStream ros = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(1024);
                try {
                    final GZIPOutputStream out = new GZIPOutputStream(ros);
                    out.write(bytes);
                    out.finish();
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, ros.size() + ""));
                    response.getOutputStream().write(ros.getInternalBuffer(), 0, ros.size());
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
                    cos = new ChunkedOutputStream(response.getOutputStream());
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

    private final HashMap<String, RemoteAPIProcess<RemoteAPIInterface>> processes  = new HashMap<String, RemoteAPIProcess<RemoteAPIInterface>>();

    private final Object                                                LOCK       = new Object();

    public RemoteAPI() {
        try {
            this.register(this);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    protected void _handleRemoteAPICall(final RemoteAPIRequest request, final RemoteAPIResponse response) throws Throwable {
        Object responseData = null;
        Object responseException = null;
        final Method method = request.getMethod();
        try {
            if (request.getIface().getRawHandler() != null) {
                /* maybe this request is handled by rawMethodHandler */
                final Object[] parameters = new Object[] { request, response };
                responseData = request.getIface().invoke(request.getIface().getRawHandler(), parameters);
                if (Boolean.TRUE.equals(responseData)) { return; }
            }
            if (method == null) { throw new ApiCommandNotAvailable(); }
            if (request.getIface().getSignatureHandler() != null && request.getIface().isSignatureRequired(method)) {
                /* maybe this request is handled by rawMethodHandler */
                final Object[] parameters = new Object[] { request, response };
                responseData = request.getIface().invoke(request.getIface().getSignatureHandler(), parameters);
                if (!Boolean.TRUE.equals(responseData)) {
                    response.setResponseCode(ResponseCode.ERROR_FORBIDDEN);
                    response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "0"));
                    return;
                }
            }
            if (!this.isAllowed(request, response)) {
                response.setResponseCode(ResponseCode.ERROR_FORBIDDEN);
                response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "0"));
                return;
            }
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
                        parameters[i] = RemoteAPI.convert(request.getParameters()[count], method.getGenericParameterTypes()[i]);
                    } catch (final Throwable e) {
                        throw new BadParameterException(request.getParameters()[count], e);
                    }
                    count++;
                }
            }
            responseData = request.getIface().invoke(method, parameters);
            if (responseIsParameter) { return; }
        } catch (final Throwable e) {
            if (this.throwException(e)) { throw e; }
            final Throwable cause = e.getCause();
            if (cause instanceof RemoteAPICustomHandler) {
                ((RemoteAPICustomHandler) cause).handle(request, response);
                return;
            } else if (cause instanceof RemoteAPIException) {
                final RemoteAPIException ex = (RemoteAPIException) cause;
                /* check if this Exception contains an API response */
                responseException = ex.getRemoteAPIExceptionResponse();
                if (responseException == null) {
                    /* we dont have an API response, use normal http stuff */
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
                }
            } else {
                /* wrap exceptions into API response */
                responseException = new RemoteAPIException(e).getRemoteAPIExceptionResponse();
            }
        }
        String text = "";
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        if (method != null && responseData != null) {
            if (RemoteAPICustomResponse.class.isAssignableFrom(responseData.getClass())) {
                ((RemoteAPICustomResponse) responseData).sendCustomResponse(request, response, ((RemoteAPICustomResponse) responseData).getResponseContent());
                return;
            }
            /* no exception thrown and method available */
            if (Clazz.isVoid(method.getReturnType())) {
                /* no content */
                response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, "0"));
            } else {
                /* we wrap response object in data:object,pid:pid(optional) */
                final HashMap<String, Object> responseJSON = new HashMap<String, Object>();
                if (RemoteAPIProcess.class.isAssignableFrom(method.getReturnType())) {
                    final RemoteAPIProcess<RemoteAPIInterface> process = (RemoteAPIProcess<RemoteAPIInterface>) responseData;
                    responseJSON.putAll(this.remoteAPIProcessResponse(process));
                    try {
                        this.registerProcess(process);
                    } catch (final Throwable e) {
                        Log.exception(e);
                        System.out.println("could not register process " + process.getPID());
                        responseJSON.put("error", "registererror");
                    }
                } else {
                    /* only data */
                    responseJSON.put("data", responseData);
                }
                if (method.getAnnotation(ApiRawJsonResponse.class) != null) {
                    text = JSonStorage.toString(responseData);
                } else {
                    text = JSonStorage.toString(responseJSON);
                }
            }
        } else {
            /* caught an exception */
            text = JSonStorage.toString(responseException);
        }
        if (request.getJqueryCallback() != null) {
            /* wrap response into a valid jquery callback response format */
            final StringBuilder sb = new StringBuilder();
            sb.append(request.getJqueryCallback());
            sb.append("(");
            sb.append(text);
            sb.append(");");
            text = sb.toString();
        }
        final byte[] bytes = text.getBytes("UTF-8");
        RemoteAPI.sendBytes(response, RemoteAPI.gzip(request), true, bytes);
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
        return new RemoteAPIRequest(interfaceHandler, intf[2], parameters.toArray(new String[] {}), request, jqueryCallback, signature);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.RemoteAPIProcessList#list()
     */
    @Override
    public ArrayList<HashMap<String, String>> list() {
        final ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
        synchronized (this.LOCK) {
            for (final Entry<String, RemoteAPIProcess<RemoteAPIInterface>> next : this.processes.entrySet()) {
                final HashMap<String, String> data = new HashMap<String, String>();
                data.put("pid", next.getValue().getPID());
                data.put("status", next.getValue().getStatus().name());
                ret.add(data);
            }
        }
        return ret;
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

    @SuppressWarnings("unchecked")
    protected void registerProcess(final RemoteAPIProcess<RemoteAPIInterface> process) throws ParseException {
        synchronized (this.LOCK) {
            if (process.isFinished()) { return; }
            process.setRemoteAPI(this);
            this.processes.put(process.getPID(), process);
            final String namespace = "processes/" + process.getPID();
            Class<?> clazz = process.getClass();
            final java.util.List<Class<?>> interfaces = new ArrayList<Class<?>>();
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
                        int defaultAuthLevel = 0;
                        final ApiAuthLevel b = c.getAnnotation(ApiAuthLevel.class);
                        if (b != null) {
                            defaultAuthLevel = b.value();
                        }

                        System.out.println("Register:   " + c.getName() + "->" + namespace);
                        try {
                            InterfaceHandler<RemoteAPIInterface> handler = this.interfaces.get(namespace);
                            if (handler == null) {
                                handler = InterfaceHandler.create((Class<RemoteAPIInterface>) c, process, defaultAuthLevel);
                                handler.setSessionRequired(c.getAnnotation(ApiSessionRequired.class) != null);
                                this.interfaces.put(namespace, handler);
                            } else {
                                handler.add((Class<RemoteAPIInterface>) c, process, defaultAuthLevel);
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

    protected HashMap<String, Object> remoteAPIProcessResponse(final RemoteAPIProcess<?> process) {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("data", process.getResponse());
        ret.put("pid", process.getPID());
        return ret;
    }

    /**
     * @param e
     * @return
     */
    protected boolean throwException(final Throwable e) {
        return false;
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

    public void unregisterProcess(final RemoteAPIProcess<?> process) {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        synchronized (this.LOCK) {
            this.processes.remove(process.getPID());
            final String namespace = "processes/" + process.getPID();
            Class<?> clazz = process.getClass();
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
                        this.interfaces.remove(namespace);
                        System.out.println("UnRegister: " + c.getName() + "->" + namespace);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }
}
