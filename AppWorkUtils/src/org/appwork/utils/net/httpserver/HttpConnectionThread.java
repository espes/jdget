/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daniel
 * 
 */
public class HttpConnectionThread extends Thread {

    private static AtomicInteger HTTPCONNECTIONTHREADS = new AtomicInteger(0);
    private HttpConnection       currentConnection     = null;

    public HttpConnectionThread(final HttpServer server, final Runnable r) {
        super(r);
        this.setName("HttpConnection:" + HttpConnectionThread.HTTPCONNECTIONTHREADS.incrementAndGet() + ":" + server.getPort() + ":" + server.isLocalhostOnly());
        this.setDaemon(true);
    }

    public HttpConnection getCurrentConnection() {
        return this.currentConnection;
    }

    @Override
    public void interrupt() {
        try {
            final HttpConnection lcurrentConnection = this.currentConnection;
            if (lcurrentConnection != null) {
                lcurrentConnection.closeConnection();
                lcurrentConnection.close();
            }
        } finally {
            super.interrupt();
        }
    }

    public void setCurrentConnection(final HttpConnection currentConnection) {
        this.currentConnection = currentConnection;
    }
}
