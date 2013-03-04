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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.annotations.AllowStorage;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.requests.HttpRequest;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author thomas
 * 
 */
public class InterfaceHandler<T> {
    private static Method HELP;
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
     * @param c
     * @param x
     * @return
     * @throws ParseException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static <T extends RemoteAPIInterface> InterfaceHandler<T> create(final Class<T> c, final RemoteAPIInterface x, final int defaultAuthLevel) throws ParseException, SecurityException, NoSuchMethodException {
        final InterfaceHandler<T> ret = new InterfaceHandler<T>(c, x, defaultAuthLevel);
        ret.parse();
        return ret;
    }

    private final RemoteAPIInterface                        impl;

    private final java.util.List<Class<T>>                  interfaceClasses;
    private final TreeMap<String, TreeMap<Integer, Method>> methods;
    private final HashMap<Method, Integer>                  parameterCountMap;
    private final HashMap<Method, Integer>                  methodsAuthLevel;
    private final HashMap<String, Method>                   rawMethods;
    private final HashSet<Method>                           signatureRequiredMethods;
    private Method                                          rawHandler       = null;
    private Method                                          signatureHandler = null;
    private final int                                       defaultAuthLevel;
    private boolean                                         sessionRequired  = false;

    /**
     * @param <T>
     * @param c
     * @param x
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private InterfaceHandler(final Class<T> c, final RemoteAPIInterface x, final int defaultAuthLevel) throws SecurityException, NoSuchMethodException {
        this.interfaceClasses = new ArrayList<Class<T>>();
        this.interfaceClasses.add(c);
        this.impl = x;
        this.methods = new TreeMap<String, TreeMap<Integer, Method>>();

        this.defaultAuthLevel = defaultAuthLevel;

        this.methodsAuthLevel = new HashMap<Method, Integer>();
        this.parameterCountMap = new HashMap<Method, Integer>();
        this.rawMethods = new HashMap<String, Method>();
        this.signatureRequiredMethods = new HashSet<Method>();
    }

    /**
     * @param c
     * @param defaultAuthLevel2
     * @param process
     * @throws ParseException
     */
    public void add(final Class<T> c, final RemoteAPIInterface process, final int defaultAuthLevel) throws ParseException {

        if (this.sessionRequired != (c.getAnnotation(ApiSessionRequired.class) != null)) { throw new ParseException("Check SessionRequired for " + this); }

        if (defaultAuthLevel != this.getDefaultAuthLevel()) { throw new ParseException("Check Authlevel " + c + " " + this); }
        if (process != this.impl) { throw new ParseException(process + "!=" + this.impl); }
        try {
            this.interfaceClasses.add(c);
            this.parse();
        } catch (final ParseException e) {
            this.interfaceClasses.remove(c);
            this.parse();
            throw e;
        }

    }

    public int getAuthLevel(final Method m) {
        final Integer auth = this.methodsAuthLevel.get(m);
        if (auth != null) { return auth; }
        return this.defaultAuthLevel;
    }

    public int getDefaultAuthLevel() {
        return this.defaultAuthLevel;
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
        Method ret = methodsByName.get(length);
        if (ret == null) {
            ret = this.rawMethods.get(methodName);
        }
        return ret;
    }

    /**
     * @param method
     * @return
     */
    public int getParameterCount(final Method method) {
        return this.parameterCountMap.get(method);
    }

    public Method getRawHandler() {
        return this.rawHandler;
    }

    public Method getSignatureHandler() {
        return this.signatureHandler;
    }

    public void help(final RemoteAPIRequest request, final RemoteAPIResponse response) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException, IOException {

        if ("true".equals(HttpRequest.getParameterbyKey(request.getHttpRequest(), "json"))) {
            this.helpJSON(request, response);
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (final Class<T> interfaceClass : this.interfaceClasses) {
            sb.append(interfaceClass.getName());
            sb.append("\r\n");
        }
        sb.append("\r\n");
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
                String name = m.getName();
                final ApiMethodName methodname = m.getAnnotation(ApiMethodName.class);
                if (methodname != null) {
                    name = methodname.value();
                }
                sb.append("\r\n====- " + name + " -====");
                final ApiDoc an = m.getAnnotation(ApiDoc.class);
                if (an != null) {
                    sb.append("\r\n    Description: ");
                    sb.append(an.value() + "");
                }
                // sb.append("\r\n    Description: ");

                final HashMap<Type, Integer> map = new HashMap<Type, Integer>();
                String call = "/" + name;
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

        final int length = text.getBytes("UTF-8").length;
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, length + ""));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));
        response.getOutputStream().write(text.getBytes("UTF-8"));
    }

    private void helpJSON(final RemoteAPIRequest request, final RemoteAPIResponse response) throws UnsupportedEncodingException, IOException {

        final List<RemoteAPIMethodDefinition> methodDefinitions = new ArrayList<RemoteAPIMethodDefinition>();

        Entry<String, TreeMap<Integer, Method>> next;
        for (final Iterator<Entry<String, TreeMap<Integer, Method>>> it = this.methods.entrySet().iterator(); it.hasNext();) {
            next = it.next();
            for (final Method m : next.getValue().values()) {
                final RemoteAPIMethodDefinition mDef = new RemoteAPIMethodDefinition();
                mDef.setMethodName(m.getName());

                final ApiDoc an = m.getAnnotation(ApiDoc.class);
                if (an != null) {
                    mDef.setDescription(an.value());
                }

                final List<String> parameters = new ArrayList<String>();

                for (int i = 0; i < m.getGenericParameterTypes().length; i++) {
                    if (m.getParameterTypes()[i] == RemoteAPIRequest.class || m.getParameterTypes()[i] == RemoteAPIResponse.class) {
                        continue;
                    }
                    parameters.add(m.getParameterTypes()[i].getSimpleName());
                }
                mDef.setParameters(parameters);

                methodDefinitions.add(mDef);
            }
        }

        final String responseText = JSonStorage.serializeToJson(methodDefinitions);

        response.setResponseCode(ResponseCode.SUCCESS_OK);

        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, responseText.getBytes("UTF-8").length + ""));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text"));

        response.getOutputStream().write(responseText.getBytes("UTF-8"));
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
     * @return the sessionRequired
     */
    public boolean isSessionRequired() {
        return this.sessionRequired;
    }

    public boolean isSignatureRequired(final Method m) {
        return this.signatureRequiredMethods.contains(m);
    }

    /**
     * @throws ParseException
     * 
     */
    private void parse() throws ParseException {
        this.methods.clear();
        this.rawMethods.clear();
        this.parameterCountMap.clear();
        this.methodsAuthLevel.clear();
        TreeMap<Integer, Method> map;
        this.methods.put("help", map = new TreeMap<Integer, Method>());
        map.put(0, InterfaceHandler.HELP);
        this.parameterCountMap.put(InterfaceHandler.HELP, 0);
        this.methodsAuthLevel.put(InterfaceHandler.HELP, 0);
        this.signatureHandler = null;
        this.rawHandler = null;
        Class<T> signatureHandlerNeededClass = null;
        for (final Class<T> interfaceClass : this.interfaceClasses) {
            for (final Method m : interfaceClass.getMethods()) {
                final ApiHiddenMethod hidden = m.getAnnotation(ApiHiddenMethod.class);
                if (hidden != null) {
                    continue;
                }
                final ApiSignatureRequired signature = m.getAnnotation(ApiSignatureRequired.class);
                this.validateMethod(m);
                int l = 0;
                for (final Class<?> c : m.getParameterTypes()) {
                    if (c != RemoteAPIRequest.class && c != RemoteAPIResponse.class) {
                        l++;
                    }
                }
                String name = m.getName();
                if ("handleRAWRemoteAPI".equals(name) && l == 0) {
                    this.rawHandler = m;
                    continue;
                } else if ("handleRemoteAPISignature".equals(name) && l == 0) {
                    this.signatureHandler = m;
                    continue;
                }
                final ApiMethodName methodname = m.getAnnotation(ApiMethodName.class);
                if (methodname != null) {
                    name = methodname.value();
                }
                TreeMap<Integer, Method> methodsByName = this.methods.get(name);
                if (methodsByName == null) {
                    methodsByName = new TreeMap<Integer, Method>();
                    this.methods.put(name, methodsByName);
                }
                if (m.getAnnotation(ApiRawMethod.class) != null) {
                    this.rawMethods.put(name, m);
                }
                this.parameterCountMap.put(m, l);
                if (methodsByName.containsKey(l)) { throw new ParseException(interfaceClass + " Contains ambiguous methods: \r\n" + m + "\r\n" + methodsByName.get(l)); }
                methodsByName.put(l, m);
                final ApiAuthLevel auth = m.getAnnotation(ApiAuthLevel.class);
                if (auth != null) {
                    this.methodsAuthLevel.put(m, auth.value());
                }
                if (signature != null) {
                    signatureHandlerNeededClass = interfaceClass;
                    this.signatureRequiredMethods.add(m);
                }
            }
        }
        if (signatureHandlerNeededClass != null && this.signatureHandler == null) { throw new ParseException(signatureHandlerNeededClass + " Contains methods that need validated Signatures but no Validator provided"); }
    }

    /**
     * @param sessionRequired
     *            the sessionRequired to set
     * @throws ParseException
     */
    protected void setSessionRequired(final boolean sessionRequired) throws ParseException {

        this.sessionRequired = sessionRequired;
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
            if (m.getGenericReturnType() != void.class && m.getGenericReturnType() != Void.class) {
                if (!RemoteAPIRawInterface.class.isAssignableFrom(m.getDeclaringClass()) && !RemoteAPISignatureHandler.class.isAssignableFrom(m.getDeclaringClass())) { throw new ParseException("Response in Parameters. " + m + " must return void, and has to handle the response itself"); }
            }
        } else {
            try {
                if (RemoteAPICustomResponse.class.isAssignableFrom(m.getReturnType())) { return; }
                if (RemoteAPIProcess.class.isAssignableFrom(m.getReturnType())) {
                    final Type t = m.getReturnType().getGenericSuperclass();
                    if (t instanceof ParameterizedTypeImpl) {
                        final ParameterizedTypeImpl p = (ParameterizedTypeImpl) t;
                        final Type[] oo = p.getActualTypeArguments();
                        for (final Type o : oo) {
                            try {
                                JSonStorage.canStore(o);
                            } catch (final InvalidTypeException e) {
                                final AllowStorage allow = m.getAnnotation(AllowStorage.class);
                                boolean found = false;
                                if (allow != null) {
                                    for (final Class<?> c : allow.value()) {
                                        if (e.getType() == c) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if (!found) { throw new InvalidTypeException(e); }
                            }
                        }
                    } else {
                        throw new ParseException("return Type of " + m + " is invalid");
                    }
                } else {
                    try {
                        JSonStorage.canStore(m.getGenericReturnType());
                    } catch (final InvalidTypeException e) {
                        final AllowStorage allow = m.getAnnotation(AllowStorage.class);
                        boolean found = false;
                        if (allow != null) {
                            for (final Class<?> c : allow.value()) {
                                if (e.getType() == c) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) { throw new InvalidTypeException(e); }
                    }
                }
            } catch (final InvalidTypeException e) {
                throw new ParseException("return Type of " + m + " is invalid", e);
            }
        }

    }

}
