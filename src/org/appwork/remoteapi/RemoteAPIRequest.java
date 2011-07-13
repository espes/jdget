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

import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;

/**
 * @author daniel
 * 
 */
public class RemoteAPIRequest {

    private final InterfaceHandler<?> iface;

    private final String[]            parameters;
    private final HttpRequest         request;

    private Method                    method;

    private int                       parameterCount;

    private final String              jqueryCallback;

    public RemoteAPIRequest(final InterfaceHandler<?> iface, final String methodName, final String[] parameters, final HttpRequest request, final String jqueryCallback) {
        this.iface = iface;
        this.parameters = parameters;
        this.request = request;
        this.jqueryCallback = jqueryCallback;
        this.method = this.iface.getMethod(methodName, this.parameters.length);
        try {
            this.parameterCount = iface.getParameterCount(this.method);
        } catch (final Throwable e) {
            this.method = null;
        }
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

    public int getParameterCount() {
        return this.parameterCount;
    }

    public String[] getParameters() {
        return this.parameters;
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

}
