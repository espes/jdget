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

import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;

/**
 * @author daniel
 * 
 */
public class HttpServerController {

    private final java.util.List<HttpServer> servers = new ArrayList<HttpServer>();

    public HttpServerController() {
    }

    public synchronized HttpHandlerInfo registerRequestHandler(final int port, final boolean localhost, final HttpRequestHandler handler) throws IOException {
        HttpServer server = null;
        for (final HttpServer s : servers) {
            if (s.getPort() == port) {
                server = s;
                break;
            }
        }
        if (server == null) {
            server = createServer(port);
            server.setLocalhostOnly(localhost);
            server.start();
            servers.add(server);
        }
        if (localhost == false && server.isLocalhostOnly()) {
            server.shutdown();
            server.setLocalhostOnly(false);
            server.start();
        }
        server.registerRequestHandler(handler);
        return new HttpHandlerInfo(server, handler) {
            @Override
            public void unregisterRequestHandler() {
                HttpServerController.this.unregisterRequestHandler(this);
            }
        };
    }

    protected HttpServer createServer(final int port) {
        return new HttpServer(port);
    }

    public synchronized void unregisterRequestHandler(final HttpHandlerInfo handlerInfo) {
        if (servers.contains(handlerInfo.getHttpServer())) {
            handlerInfo.getHttpServer().unregisterRequestHandler(handlerInfo.getHttpHandler());
            if (handlerInfo.getHttpServer().getHandler().isEmpty()) {
                servers.remove(handlerInfo.getHttpServer());
                handlerInfo.getHttpServer().shutdown();
            }
        }
    }
}
