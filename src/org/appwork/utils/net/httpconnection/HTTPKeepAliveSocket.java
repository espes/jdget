/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpconnection;

import java.net.Socket;

/**
 * @author daniel
 * 
 */
public class HTTPKeepAliveSocket {

    private final Socket socket;

    public Socket getSocket() {
        return this.socket;
    }

    public long getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }

    public long getRequestsLeft() {
        return Math.max(0, this.getRequestsMax() - this.requests);
    }

    public long getRequestsMax() {
        return this.maxRequests;
    }

    public void increaseRequests() {
        this.requests += 1;
    }

    private final long    keepAliveTimeout;
    private final long    maxRequests;
    private volatile long keepAliveTimestamp = -1;
    private volatile long requests           = 0;
    private final String  ID;

    public String getID() {
        return this.ID;
    }

    public long getKeepAliveTimestamp() {
        return this.keepAliveTimestamp;
    }

    public void keepAlive() {
        this.keepAliveTimestamp = System.currentTimeMillis() + this.getKeepAliveTimeout();
    }

    public HTTPKeepAliveSocket(final String ID, final Socket socket, final long keepAliveTimeout, final long maxRequests) {
        this.ID = ID;
        this.socket = socket;
        this.keepAliveTimeout = Math.max(0, keepAliveTimeout);
        this.maxRequests = Math.max(0, maxRequests);
    }
}
