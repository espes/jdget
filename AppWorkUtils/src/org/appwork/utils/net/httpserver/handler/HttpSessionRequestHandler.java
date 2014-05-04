/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.handler;

import org.appwork.remoteapi.exceptions.BasicRemoteAPIException;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public interface HttpSessionRequestHandler<T extends HttpSession> {

    public abstract boolean onGetSessionRequest(final T session, final GetRequest request, final HttpResponse response) throws BasicRemoteAPIException;

    public abstract boolean onPostSessionRequest(final T session, final PostRequest request, final HttpResponse response) throws BasicRemoteAPIException;

}
