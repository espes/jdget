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

import org.appwork.remoteapi.exceptions.BasicRemoteAPIException;
import org.appwork.remoteapi.exceptions.SessionException;
import org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class SessionRemoteAPI<T extends HttpSession> extends RemoteAPI implements HttpSessionRequestHandler<T> {

    public SessionRemoteAPI() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler#
     * onGetSessionRequest(org.appwork.utils.net.httpserver.session.HttpSession,
     * org.appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetSessionRequest(final T session, final GetRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        RemoteAPIRequest apiRequest = getInterfaceHandler(request);
        if (apiRequest == null) { return onUnknownRequest(request, response); }
        apiRequest = createSessionRemoteAPIRequest(session, request, apiRequest);
        if (apiRequest.getIface().isSessionRequired() && (session == null || !session.isAlive())) { throw new SessionException(); }
        _handleRemoteAPICall(apiRequest, createRemoteAPIResponseObject(response));
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler#
     * onPostSessionRequest
     * (org.appwork.utils.net.httpserver.session.HttpSession,
     * org.appwork.utils.net.httpserver.requests.PostRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onPostSessionRequest(final T session, final PostRequest request, final HttpResponse response) throws BasicRemoteAPIException {
        RemoteAPIRequest apiRequest = getInterfaceHandler(request);
        if (apiRequest == null) { return onUnknownRequest(request, response); }
        apiRequest = createSessionRemoteAPIRequest(session, request, apiRequest);
        if (apiRequest.getIface().isSessionRequired() && (session == null || !session.isAlive())) { throw new SessionException(); }
        _handleRemoteAPICall(apiRequest, createRemoteAPIResponseObject(response));
        return true;
    }

    protected SessionRemoteAPIRequest<T> createSessionRemoteAPIRequest(final T session, final HttpRequest request, final RemoteAPIRequest apiRequest) {
        return new SessionRemoteAPIRequest<T>(request, apiRequest, session);
    }

}
