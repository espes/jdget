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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;

/**
 * @author daniel
 * 
 */
public class HttpServer implements Runnable {

    private final int                     port;
    private ServerSocket                  controlSocket;
    private Thread                        controlThread = null;
    private ThreadGroup                   threadGroup   = null;
    private boolean                       localhostOnly = false;
    private boolean                       debug         = false;
    private ArrayList<HttpRequestHandler> handler       = null;

    public HttpServer(final int port) {
        this.port = port;
        this.threadGroup = new ThreadGroup("HttpServer");
        this.handler = new ArrayList<HttpRequestHandler>();
    }

    public ArrayList<HttpRequestHandler> getHandler() {
        return this.handler;
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

    /**
     * @return the port
     */
    public int getPort() {
        return this.port;
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

    public boolean isRunning() {
        return this.controlThread != null;
    }

    /*
     * to register a new handler we create a copy of current handlerList and
     * then add new handler to it and set it as new handlerList. by doing so,
     * all current connections dont have to sync on their handlerlist
     */
    public HttpHandlerInfo registerRequestHandler(final HttpRequestHandler handler) {
        synchronized (this.handler) {
            if (!this.handler.contains(handler)) {
                final ArrayList<HttpRequestHandler> newhandler = new ArrayList<HttpRequestHandler>(this.handler);
                newhandler.add(handler);
                this.handler = newhandler;
            }
            return new HttpHandlerInfo(this, handler);
        }
    }

    public void run() {
        final Thread current = this.controlThread;
        final ServerSocket socket = this.controlSocket;
        try {
            socket.setSoTimeout(5 * 60 * 1000);
        } catch (final SocketException e1) {
            e1.printStackTrace();
        }
        try {
            while (true) {
                try {
                    final Socket clientSocket = socket.accept();
                    try {
                       
                        new HttpConnection(this, clientSocket);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                } catch (final SocketTimeoutException e) {
                    /*
                     * nothing, our 5 mins connect timeout for the http server
                     * socket
                     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */

    /**
     * @param localhostOnly
     *            the localhostOnly to set
     */
    public void setLocalhostOnly(final boolean localhostOnly) {
        this.localhostOnly = localhostOnly;
    }

    public synchronized void shutdown() {
        try {
            this.controlSocket.close();
        } catch (final Throwable e) {
        }
        this.controlThread = null;
    }

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
        this.controlThread.setName("HttpServerThread:" + this.port + ":" + this.localhostOnly);
        this.controlThread.start();
    }

    public synchronized void stop() {
        try {
            this.controlSocket.close();
        } catch (final Throwable e) {
        }
        this.threadGroup.interrupt();
        this.controlThread = null;
    }

    /*
     * to unregister a new handler we create a copy of current handlerList and
     * then remove handler to it and set it as new handlerList. by doing so, all
     * current connections dont have to sync on their handlerlist
     */
    public void unregisterRequestHandler(final HttpRequestHandler handler) {
        synchronized (this.handler) {
            final ArrayList<HttpRequestHandler> newhandler = new ArrayList<HttpRequestHandler>(this.handler);
            newhandler.remove(handler);
            this.handler = newhandler;
        }
    }

}
