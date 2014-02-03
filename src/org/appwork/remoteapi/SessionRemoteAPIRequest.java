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

    private final T          session;
    private RemoteAPIRequest apiRequest;

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
        return apiRequest;
    }



    @Override
    public InterfaceHandler<?> getIface() {

        return apiRequest.getIface();
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return apiRequest.getInputStream();
    }

    @Override
    public String getJqueryCallback() {

        return apiRequest.getJqueryCallback();
    }

    @Override
    public Method getMethod() {

        return apiRequest.getMethod();
    }

    @Override
    public String getMethodName() {

        return apiRequest.getMethodName();
    }

    @Override
    public int getParameterCount() {

        return apiRequest.getParameterCount();
    }

    @Override
    public String[] getParameters() {

        return apiRequest.getParameters();
    }

//    @Override
//    public List<KeyValuePair> getPostParameter() throws IOException {
//
//        return apiRequest.getPostParameter();
//    }

    @Override
    public List<String> getRemoteAdress() {

        return apiRequest.getRemoteAdress();
    }

    @Override
    public String getRequestedPath() {

        return apiRequest.getRequestedPath();
    }

    @Override
    public String getRequestedURL() {

        return apiRequest.getRequestedURL();
    }

    @Override
    public List<KeyValuePair> getRequestedURLParameters() {

        return apiRequest.getRequestedURLParameters();
    }

    @Override
    public HeaderCollection getRequestHeaders() {

        return apiRequest.getRequestHeaders();
    }

    @Override
    public REQUESTTYPE getRequestType() {

        return apiRequest.getRequestType();
    }



    public T getSession() {
        return this.session;
    }

}
