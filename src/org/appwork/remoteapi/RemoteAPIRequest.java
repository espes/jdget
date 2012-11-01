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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HeadRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequestInterface;
import org.appwork.utils.net.httpserver.requests.PostRequest;

/**
 * @author daniel
 * 
 */
public class RemoteAPIRequest implements HttpRequestInterface {

    public static enum REQUESTTYPE {
        HEAD,
        POST,
        GET,
        UNKNOWN
    }

    private final InterfaceHandler<?> iface;

    private final String[]            parameters;
    private final HttpRequest         request;

    private Method                    method;

    private int                       parameterCount;

    private final String              jqueryCallback;

    private final String              methodName;

    public RemoteAPIRequest(final InterfaceHandler<?> iface, final String methodName, final String[] parameters, final HttpRequest request, final String jqueryCallback) {
        this.iface = iface;
        this.parameters = parameters;
        this.request = request;
        this.jqueryCallback = jqueryCallback;
        this.methodName = methodName;

        this.method = this.iface.getMethod(methodName, this.parameters.length);
        try {
            this.parameterCount = iface.getParameterCount(this.method);
        } catch (final Throwable e) {
            this.method = null;
        }
    }

    public HttpRequest getHttpRequest() {
        return this.request;
    }

    public InterfaceHandler<?> getIface() {
        return this.iface;
    }

    public InputStream getInputStream() throws IOException {
        if (this.request instanceof PostRequest) { return ((PostRequest) this.request).getInputStream(); }
        return null;
    }

    /**
     * @return the jqueryCallback
     */
    public String getJqueryCallback() {
        return this.jqueryCallback;
    }

    /**
     * @return
     */
    public Method getMethod() {

        return this.method;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return this.methodName;
    }

    public int getParameterCount() {
        return this.parameterCount;
    }

    public String[] getParameters() {
        return this.parameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#
     * getPostParameter()
     */
    @Override
    public LinkedList<String[]> getPostParameter() throws IOException {
        return this.request.getPostParameter();
    }

    /**
     * @return
     */
    public List<String> getRemoteAdress() {
        return this.request.getRemoteAddress();
    }

    public String getRequestedPath() {
        return this.request.getRequestedPath();
    }

    public String getRequestedURL() {
        return this.request.getRequestedURL();
    }

    /**
     * @return the requestedURLParameters
     */
    public LinkedList<String[]> getRequestedURLParameters() {
        return this.request.getRequestedURLParameters();
    }

    public HeaderCollection getRequestHeaders() {
        return this.request.getRequestHeaders();
    }

    public REQUESTTYPE getRequestType() {
        if (this.request instanceof HeadRequest) { return REQUESTTYPE.HEAD; }
        if (this.request instanceof PostRequest) { return REQUESTTYPE.POST; }
        if (this.request instanceof GetRequest) { return REQUESTTYPE.GET; }
        return REQUESTTYPE.UNKNOWN;
    }

}
