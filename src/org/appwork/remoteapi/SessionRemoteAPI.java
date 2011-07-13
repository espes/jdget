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

import org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class SessionRemoteAPI<T extends HttpSession> extends RemoteAPI implements HttpSessionRequestHandler<T> {

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler#
     * onGetSessionRequest(org.appwork.utils.net.httpserver.session.HttpSession,
     * org.appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetSessionRequest(final T session, final GetRequest request, final HttpResponse response) {
        RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }
        apiRequest = new SessionRemoteAPIRequest<T>(request, apiRequest, session);
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
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
    public boolean onPostSessionRequest(final T session, final PostRequest request, final HttpResponse response) {
        RemoteAPIRequest apiRequest = this.getInterfaceHandler(request);
        if (apiRequest == null) { return false; }
        apiRequest = new SessionRemoteAPIRequest<T>(request, apiRequest, session);
        try {
            this._handleRemoteAPICall(apiRequest, new RemoteAPIResponse(response));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
