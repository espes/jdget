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

/**
 * @author daniel
 * 
 */
public class ConnectionLimiter {

    private static final Object LOCK = new Object();
    private int connectioncount;
    private ArrayList<Long> list = new ArrayList<Long>();
    private int maxConcurrent = -1;
    private int timeConnections = -1;
    private long timeTime = -1;

    public ConnectionLimiter() {
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
        maxConcurrent = max;
    }

    /**
     * returns current maximum of allowed concurrent connections or -1 for
     * unlimited
     * 
     * @return
     */
    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    /**
     * DON'T forget to call this after connection is closed
     */
    public void closedConnection() {
        synchronized (LOCK) {
            connectioncount--;
        }
    }

    /**
     * get current allowed connection in specific time or -1,-1 if none set
     * 
     * @return
     */
    public long[] getConnectionTimeLimit() {
        return new long[] { timeConnections, timeTime };
    }

    /**
     * set max allowed connections in specific time (ms)
     * 
     * @param connections
     * @param time
     */
    public synchronized void setConnectionTimeLimit(int connections, long time) {
        if (connections > 0 && time > 0) {
            timeConnections = connections;
            timeTime = time;
        } else {
            timeConnections = -1;
            timeTime = -1;
        }
    }

    /**
     * call this if before you open a new connection
     * 
     * @throws InterruptedException
     */
    public synchronized void openedConnection() throws InterruptedException {
        if (maxConcurrent > 0) {
            while (true) {
                synchronized (LOCK) {
                    if (connectioncount < maxConcurrent) break;
                }
                // System.out.println("block 250 ms for " + maxConcurrent +
                // " connectionlimit");
                Thread.sleep(250);
            }
        }
        if (timeConnections > 0) {
            while (true) {
                Iterator<Long> it = list.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if ((it.next() + timeTime) < System.currentTimeMillis()) {
                            it.remove();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (list.size() >= timeConnections) {
                    long wait = timeConnections;
                    if (it.hasNext()) {
                        /* calculate how long we have to wait */
                        wait = Math.max(250, it.next() - (System.currentTimeMillis() - timeTime));
                    }
                    // System.out.println("wait " + wait + " ms because we got "
                    // + list.size() + " connections the last minute");
                    Thread.sleep(wait);
                } else {
                    break;
                }
            }
            list.add(System.currentTimeMillis());
            // System.out.println(list.size());
        }
        synchronized (LOCK) {
            connectioncount++;
        }
    }
}
