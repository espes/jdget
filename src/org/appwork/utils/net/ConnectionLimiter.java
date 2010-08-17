/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.util.ArrayList;
import java.util.Iterator;

import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public class ConnectionLimiter {

    private static final Object LOCK = new Object();
    private int connectioncount;
    private final ArrayList<Long> list = new ArrayList<Long>();
    private int maxConcurrent = -1;
    private int timeConnections = -1;
    private long timeTime = -1;

    public ConnectionLimiter() {
    }

    /**
     * DON'T forget to call this after connection is closed
     */
    public void closedConnection() {
        synchronized (ConnectionLimiter.LOCK) {
            this.connectioncount--;
        }
    }

    /**
     * get current allowed connection in specific time or -1,-1 if none set
     * 
     * @return
     */
    public long[] getConnectionTimeLimit() {
        return new long[] { this.timeConnections, this.timeTime };
    }

    /**
     * returns current maximum of allowed concurrent connections or -1 for
     * unlimited
     * 
     * @return
     */
    public int getMaxConcurrent() {
        return this.maxConcurrent;
    }

    /**
     * call this if before you open a new connection
     * 
     * @throws InterruptedException
     */
    public synchronized void openedConnection() throws InterruptedException {
        if (this.maxConcurrent > 0) {
            while (true) {
                synchronized (ConnectionLimiter.LOCK) {
                    if (this.connectioncount < this.maxConcurrent) {
                        break;
                    }
                }
                Log.L.warning("block 250 ms for " + this.maxConcurrent + " connectionlimit");
                Thread.sleep(250);
            }
        }
        if (this.timeConnections > 0) {
            while (true) {
                final Iterator<Long> it = this.list.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next() + this.timeTime < System.currentTimeMillis()) {
                            it.remove();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (this.list.size() >= this.timeConnections) {
                    long wait = this.timeConnections;
                    if (it.hasNext()) {
                        /* calculate how long we have to wait */
                        wait = Math.max(250, it.next() - (System.currentTimeMillis() - this.timeTime));
                    }
                    Log.L.warning("wait " + wait + " ms because we got " + this.list.size() + " connections the last minute");
                    Thread.sleep(wait);
                } else {
                    break;
                }
            }
            this.list.add(System.currentTimeMillis());
            // System.out.println(list.size());
        }
        synchronized (ConnectionLimiter.LOCK) {
            this.connectioncount++;
        }
    }

    /**
     * set max allowed connections in specific time (ms)
     * 
     * @param connections
     * @param time
     */
    public synchronized void setConnectionTimeLimit(final int connections, final long time) {
        if (connections > 0 && time > 0) {
            this.timeConnections = connections;
            this.timeTime = time;
        } else {
            this.timeConnections = -1;
            this.timeTime = -1;
        }
    }

    /**
     * set max allowed concurrent connections
     * 
     * @param max
     */
    public synchronized void setMaxConcurrent(int max) {
        if (max <= 0) {
            max = -1;
        }
        this.maxConcurrent = max;
    }
}
