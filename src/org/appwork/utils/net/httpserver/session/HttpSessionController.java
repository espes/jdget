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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIUnauthorizedException;
import org.appwork.remoteapi.SessionRemoteAPIRequest;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.handler.HttpSessionRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public abstract class HttpSessionController<T extends HttpSession> implements HttpRequestHandler, LoginAPIInterface {

    private java.util.List<HttpSessionRequestHandler<T>> handler = null;

    public HttpSessionController() {
        this.handler = new ArrayList<HttpSessionRequestHandler<T>>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean disconnect(final RemoteAPIRequest request) {
        final SessionRemoteAPIRequest<T> req = (SessionRemoteAPIRequest<T>) request;
        final T session = req.getSession();
        if (session != null) { return this.removeSession(session); }
        return false;
    }

    private String extractSessionID(final LinkedList<String[]> params) {
        final Iterator<String[]> it = params.iterator();
        while (it.hasNext()) {
            final String[] next = it.next();
            if ("token".equalsIgnoreCase(next[0])) {
                it.remove();
                return next[1];
            }
        }
        return null;
    }

    /**
     * get session for given sessionID or null in case session is invalid/not
     * found
     * 
     * @param request
     * @param sessionID
     * @return
     */
    protected abstract T getSession(final String sessionID);

    @Override
    public String handshake(final String user, final String password) {
        final T session = this.newSession(user, password);
        if (session == null) { throw new RemoteAPIUnauthorizedException(); }
        return session.getSessionID();
    }

    /**
     * create new session for given username, password.
     * 
     * @param username
     * @param password
     * @return
     */
    protected abstract T newSession(String username, String password);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.handler.HttpRequestHandler#onGetRequest
     * (org.appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        final java.util.List<HttpSessionRequestHandler<T>> handlers = this.handler;
        final T session = this.getSession(this.extractSessionID(request.getRequestedURLParameters()));
        for (final HttpSessionRequestHandler<T> handler : handlers) {
            if (handler.onGetSessionRequest(session, request, response)) { return true; }
        }
        return false;
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
    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        final java.util.List<HttpSessionRequestHandler<T>> handlers = this.handler;
        final T session = this.getSession(this.extractSessionID(request.getRequestedURLParameters()));
        for (final HttpSessionRequestHandler<T> handler : handlers) {
            if (handler.onPostSessionRequest(session, request, response)) { return true; }
        }
        return false;
    }

    public void registerSessionRequestHandler(final HttpSessionRequestHandler<T> handler) {
        synchronized (this.handler) {
            if (!this.handler.contains(handler)) {
                final java.util.List<HttpSessionRequestHandler<T>> newhandler = new ArrayList<HttpSessionRequestHandler<T>>(this.handler);
                newhandler.add(handler);
                this.handler = newhandler;
            }
        }
    }

    protected abstract boolean removeSession(final T session);

    public void unregisterSessionRequestHandler(final HttpSessionRequestHandler<T> handler) {
        synchronized (this.handler) {
            final java.util.List<HttpSessionRequestHandler<T>> newhandler = new ArrayList<HttpSessionRequestHandler<T>>(this.handler);
            newhandler.remove(handler);
            this.handler = newhandler;
        }
    }

}
