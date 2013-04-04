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

import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class SessionRemoteAPIRequest<T extends HttpSession> extends RemoteAPIRequest {

    private final T session;

    /**
     * @param apiRequest
     * @param session
     * @return
     */
    protected SessionRemoteAPIRequest(final HttpRequest request, final RemoteAPIRequest apiRequest, final T session) {
        super(apiRequest.getIface(), apiRequest.getMethodName(), apiRequest.getParameters(), request, apiRequest.getJqueryCallback(), apiRequest.getSignature(), apiRequest.getRequestID());
        this.session = session;
    }

    public T getSession() {
        return this.session;
    }

}
