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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class HttpServer implements Runnable {

    private final int                      port;
    private ServerSocket                   controlSocket;
    private Thread                         controlThread = null;
    private ThreadGroup                    threadGroup   = null;
    private boolean                        localhostOnly = false;
    private boolean                        debug         = false;
    private LinkedList<HttpConnection>     connections   = null;
    private LinkedList<HttpRequestHandler> handler       = null;

    public HttpServer(final int port) {
        this.port = port;
        this.threadGroup = new ThreadGroup("HttpServer");
        this.connections = new LinkedList<HttpConnection>();
        this.handler = new LinkedList<HttpRequestHandler>();
    }

    protected InetAddress getLocalHost() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("127.0.0.1");
        } catch (final UnknownHostException e1) {
        }
        if (localhost != null) { return localhost; }
        try {
            localhost = InetAddress.getByName(null);
        } catch (final UnknownHostException e1) {
        }
        return localhost;
    }

    protected HttpRequestHandler getRequestHandler(final HttpRequest request) {
        synchronized (this.handler) {
            for (final HttpRequestHandler h : this.handler) {
                if (h.canHandle(request)) { return h; }
            }
        }
        /* generate error handler */
        return new HttpRequestHandler() {

            @Override
            public boolean canHandle(final HttpRequest request) {
                return true;
            }

            @Override
            public void onGetRequest(final GetRequest request, final HttpResponse response) {
                response.setResponseCode(ResponseCode.SERVERERROR_NOT_IMPLEMENTED);
            }

            @Override
            public void onPostRequest(final PostRequest request, final HttpResponse response) {
                response.setResponseCode(ResponseCode.SERVERERROR_NOT_IMPLEMENTED);
            }

        };
    }

    /**
     * @return the clientThreadGroup
     */
    protected ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * @return the localhostOnly
     */
    public boolean isLocalhostOnly() {
        return this.localhostOnly;
    }

    public void registerRequestHandler(final HttpRequestHandler handler) {
        synchronized (this.handler) {
            this.handler.add(handler);
        }
    }

    protected void removeConnection(final HttpConnection connection) {
        synchronized (this.connections) {
            this.connections.remove(connection);
        }
    }

    public void run() {
        final Thread current = this.controlThread;
        final ServerSocket socket = this.controlSocket;
        try {
            while (true) {
                try {
                    final Socket clientSocket = socket.accept();
                    final HttpConnection connection = new HttpConnection(this, clientSocket);
                    synchronized (this.connections) {
                        this.connections.add(connection);
                    }
                } catch (final IOException e) {
                    break;
                }
                if (current == null || current.isInterrupted()) {
                    break;
                }
            }
        } finally {
            try {
                socket.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param debug
     *            the debug to set
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * @param localhostOnly
     *            the localhostOnly to set
     */
    public void setLocalhostOnly(final boolean localhostOnly) {
        this.localhostOnly = localhostOnly;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */

    public synchronized void start() throws IOException {
        if (this.isLocalhostOnly()) {
            /* we only want localhost bound here */
            final SocketAddress socketAddress = new InetSocketAddress(this.getLocalHost(), this.port);
            this.controlSocket = new ServerSocket();
            this.controlSocket.bind(socketAddress);
        } else {
            this.controlSocket = new ServerSocket(this.port);
        }
        this.controlThread = new Thread(this.threadGroup, this);
        this.controlThread.setName("HttpServerThread");
        this.controlThread.start();
    }

    public synchronized void stop() {
        try {
            this.controlSocket.close();
        } catch (final Throwable e) {
        }
        this.threadGroup.interrupt();
    }

    public void unregisterRequestHandler(final HttpRequestHandler handler) {
        synchronized (this.handler) {
            this.handler.remove(handler);
        }
    }
}
