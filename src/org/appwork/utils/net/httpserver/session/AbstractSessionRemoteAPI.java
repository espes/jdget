/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.session;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.appwork.remoteapi.ParseException;
import org.appwork.remoteapi.RemoteAPI;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.SessionRemoteAPIRequest;
import org.appwork.remoteapi.exceptions.AuthException;
import org.appwork.remoteapi.exceptions.BasicRemoteAPIException;
import org.appwork.remoteapi.exceptions.InternalApiException;
import org.appwork.remoteapi.exceptions.SessionException;
import org.appwork.remoteapi.responsewrapper.DataObject;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.KeyValuePair;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.reflection.Clazz;

import sun.security.action.GetLongAction;

/**
 * @author daniel
 * 
 */
public abstract class AbstractSessionRemoteAPI<T extends HttpSession> extends RemoteAPI implements HttpRequestHandler, LoginAPIInterface {

    protected SessionRemoteAPIRequest<T> createSessionRemoteAPIRequest(final T session, final HttpRequest request, final RemoteAPIRequest apiRequest) {
        return new SessionRemoteAPIRequest<T>(request, apiRequest, session);
    }

    public AbstractSessionRemoteAPI() throws ParseException {
        // this.handler = new ArrayList<HttpSessionRequestHandler<T>>();
        register(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean disconnect(final RemoteAPIRequest request) {
        final SessionRemoteAPIRequest<T> req = (SessionRemoteAPIRequest<T>) request;
        final T session = req.getSession();
        if (session != null) { return this.removeSession(session); }
        return false;
    }

    private String extractSessionID(final HttpRequest request) {
        ArrayList<KeyValuePair> params = new ArrayList<KeyValuePair>();
        String token = null;
        for (KeyValuePair next : request.getRequestedURLParameters()) {

            if ("token".equalsIgnoreCase(next.key)) {

                token = next.value;
            } else {
                params.add(next);
            }
        }
        request.setRequestedURLParameters(params);
        return token;
    }

    /**
     * get session for given sessionID or null in case session is invalid/not
     * found
     * 
     * @param request
     * @param sessionID
     * @return
     */
    protected abstract T getSession(org.appwork.utils.net.httpserver.requests.HttpRequest request, final String sessionID);

    @Override
    public String handshake(final RemoteAPIRequest request, final String user, final String password) throws AuthException {
        final T session = this.newSession(request, user, password);
        if (session == null) { throw new AuthException(); }
        return session.getSessionID();
    }

    /**
     * create new session for given username, password.
     * 
     * @param username
     * @param password
     * @return
     */
    protected abstract T newSession(final RemoteAPIRequest request, String username, String password);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.handler.HttpRequestHandler#onGetRequest
     * (org.appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        return onRequest(request, response);

    }
    
    protected Object handleVoidMethods(Object responseData, final Method method) {
        if (Clazz.isVoid(method.getReturnType())) {
            // void return
            responseData = null;
        }
        return responseData;
    }
    public String toString(final RemoteAPIRequest request, final RemoteAPIResponse response, final Object responseData) {

        return JSonStorage.serializeToJson(responseData);
    }
    /**
     * @param request
     * @param response
     * @return
     * @throws BasicRemoteAPIException
     */
    private boolean onRequest(HttpRequest request, HttpResponse response) throws BasicRemoteAPIException {
        try {
            
            String token = this.extractSessionID(request);
            RemoteAPIRequest apiRequest = getInterfaceHandler(request);
            if (apiRequest == null || apiRequest.getMethod() == null) { return onUnknownRequest(request, response); }
            Class<?> declaringClass = apiRequest.getMethod().getDeclaringClass();
            if (declaringClass != LoginAPIInterface.class && apiRequest.getIface().isSessionRequired()) {
                // session required
                final T session = this.getSession(request, token);
                if (session == null || !session.isAlive()) { throw new SessionException(); }
                apiRequest = createSessionRemoteAPIRequest(session, request, apiRequest);
            }

            _handleRemoteAPICall(apiRequest, createRemoteAPIResponseObject(response));
            return true;
        } catch (Throwable e) {
            BasicRemoteAPIException apiException;
            if (!(e instanceof BasicRemoteAPIException)) {
                apiException = new InternalApiException(e);
            } else {
                apiException = (BasicRemoteAPIException) e;
            }

            try {
                return apiException.handle(response);
            } catch (IOException e1) {
                throw new BasicRemoteAPIException(e1);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.handler.HttpRequestHandler#onPostRequest
     * (org.appwork.utils.net.httpserver.requests.PostRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onPostRequest(final PostRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        return onRequest(request, response);

    }

    protected abstract boolean removeSession(final T session);

}
