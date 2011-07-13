/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.test;

import org.appwork.remoteapi.RemoteAPI;
import org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class SessionAPI extends RemoteAPI implements HttpSessionRequestHandler<TestSession> {

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler#
     * onGetSessionRequest(org.appwork.utils.net.httpserver.session.HttpSession,
     * org.appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetSessionRequest(final TestSession session, final GetRequest request, final HttpResponse response) {
        return super.onGetRequest(request, response);
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
    public boolean onPostSessionRequest(final TestSession session, final PostRequest request, final HttpResponse response) {
        return super.onPostRequest(request, response);
    }

}
