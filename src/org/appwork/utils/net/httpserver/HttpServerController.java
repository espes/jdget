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

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author daniel
 * 
 */
public class HttpServerController {

    private final ArrayList<HttpServer> servers = new ArrayList<HttpServer>();

    public HttpServerController() {
    }

    public synchronized HttpRequestHandlerInfo registerRequestHandler(final int port, final boolean localhost, final HttpRequestHandler handler) throws IOException {
        HttpServer server = null;
        for (final HttpServer s : this.servers) {
            if (s.getPort() == port && s.isLocalhostOnly() == localhost) {
                server = s;
                break;
            }
        }
        if (server == null) {
            server = new HttpServer(port);
            server.setLocalhostOnly(localhost);
            server.start();
            this.servers.add(server);
        }
        server.registerRequestHandler(handler);
        return new HttpRequestHandlerInfo(server, handler) {
            @Override
            public void unregisterRequestHandler() {
                HttpServerController.this.unregisterRequestHandler(this);
            }
        };
    }

    public synchronized void unregisterRequestHandler(final HttpRequestHandlerInfo handlerInfo) {
        if (this.servers.contains(handlerInfo.getHttpServer())) {
            handlerInfo.getHttpServer().unregisterRequestHandler(handlerInfo.getHttpRequestHandler());
            if (handlerInfo.getHttpServer().getHandler().isEmpty()) {
                this.servers.remove(handlerInfo.getHttpServer());
                handlerInfo.getHttpServer().shutdown();
            }
        }
    }
}
