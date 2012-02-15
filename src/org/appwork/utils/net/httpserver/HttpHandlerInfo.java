/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver;

import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;

/**
 * @author daniel
 * 
 */
public class HttpHandlerInfo {

    private final HttpServer         server;
    private final HttpRequestHandler handler;

    protected HttpHandlerInfo(final HttpServer server, final HttpRequestHandler handler) {
        this.server = server;
        this.handler = handler;
    }

    protected HttpRequestHandler getHttpHandler() {
        return this.handler;
    }

    public HttpServer getHttpServer() {
        return this.server;
    }

    public int getPort() {
        return this.server.getPort();
    }

    public boolean isLocalhostOnly() {
        return this.server.isLocalhostOnly();
    }

    public boolean isRunning() {
        return this.server.isRunning();
    }

    public void unregisterRequestHandler() {
        this.server.unregisterRequestHandler(this.handler);
    }
}
