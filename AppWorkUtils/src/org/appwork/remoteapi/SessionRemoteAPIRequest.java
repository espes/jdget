/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.KeyValuePair;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class SessionRemoteAPIRequest<T extends HttpSession> extends RemoteAPIRequest {

    private final T                session;
    private final RemoteAPIRequest apiRequest;

    /**
     * @param apiRequest
     * @param session
     * @return
     */
    public SessionRemoteAPIRequest(final HttpRequest request, final RemoteAPIRequest apiRequest, final T session) {
        super(apiRequest.getIface(), apiRequest.getMethodName(), apiRequest.getParameters(), request, apiRequest.getJqueryCallback());
        this.apiRequest = apiRequest;
        this.session = session;

    }

    public RemoteAPIRequest getApiRequest() {
        return this.apiRequest;
    }

    @Override
    public InterfaceHandler<?> getIface() {

        return this.apiRequest.getIface();
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return this.apiRequest.getInputStream();
    }

    @Override
    public String getJqueryCallback() {

        return this.apiRequest.getJqueryCallback();
    }

    @Override
    public Method getMethod() {

        return this.apiRequest.getMethod();
    }

    @Override
    public String getMethodName() {

        return this.apiRequest.getMethodName();
    }

    @Override
    public String[] getParameters() {

        return this.apiRequest.getParameters();
    }

    // @Override
    // public List<KeyValuePair> getPostParameter() throws IOException {
    //
    // return apiRequest.getPostParameter();
    // }

    @Override
    public List<String> getRemoteAdress() {

        return this.apiRequest.getRemoteAdress();
    }

    @Override
    public String getRequestedPath() {

        return this.apiRequest.getRequestedPath();
    }

    @Override
    public String getRequestedURL() {

        return this.apiRequest.getRequestedURL();
    }

    @Override
    public List<KeyValuePair> getRequestedURLParameters() {

        return this.apiRequest.getRequestedURLParameters();
    }

    @Override
    public HeaderCollection getRequestHeaders() {

        return this.apiRequest.getRequestHeaders();
    }

    @Override
    public REQUESTTYPE getRequestType() {

        return this.apiRequest.getRequestType();
    }

    public T getSession() {
        return this.session;
    }

}
