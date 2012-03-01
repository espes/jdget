/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.throttledconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.throttledconnection;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class ThrottledOutputStream extends OutputStream implements ThrottledConnection {

    private ThrottledConnectionHandler handler;
    private final OutputStream         out;

    protected volatile long            transferedCounter  = 0;
    protected volatile long            transferedCounter2 = 0;
    private volatile int               limitCurrent       = 0;
    private int                        limitCounter       = 0;

    private int                        offset;
    private int                        todo;
    private int                        rest;
    private long                       slotTimeLeft       = 0;
    private long                       lastTimeWrite      = 0;

    /**
     * constructor for not managed ThrottledOutputStream
     * 
     * @param in
     */
    public ThrottledOutputStream(final OutputStream out) {
        this.out = out;
    }

    /**
     * DO NOT FORGET TO CLOSE
     */
    @Override
    public void close() throws IOException {
        /* remove this stream from manager */
        if (this.handler != null) {
            this.handler.removeThrottledConnection(this);
            this.handler = null;
        }
        synchronized (this) {
            this.notify();
        }
        this.out.close();
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public ThrottledConnectionHandler getHandler() {
        return this.handler;
    }

    @Override
    public int getLimit() {
        return this.limitCurrent;
    }

    /**
     * set a new ThrottledConnectionManager
     * 
     * @param manager
     */
    public void setHandler(final ThrottledConnectionHandler manager) {
        if (this.handler != null && this.handler != manager) {
            this.handler.removeThrottledConnection(this);
        }
        this.handler = manager;
        if (this.handler != null) {
            this.handler.addThrottledConnection(this);
        }
    }

    /**
     * sets limit 0: no limit >0: use limit
     * 
     * @param kpsLimit
     */
    public void setLimit(final int kpsLimit) {
        if (kpsLimit == this.limitCurrent) { return; }
        /* TODO: maybe allow little jitter here */
        this.limitCurrent = Math.max(0, kpsLimit);
    }

    @Override
    public long transfered() {
        return this.transferedCounter;
    }

    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (this.limitCurrent == 0) {
            /* no limit is set */
            this.out.write(b, off, len);
            this.transferedCounter += len;
        } else {
            /* a limit is set */
            this.offset = off;
            this.rest = len;
            while (this.rest > 0) {
                /* loop until all data is written */
                this.slotTimeLeft = 0;
                if (this.limitCounter <= 0 && (this.slotTimeLeft = System.currentTimeMillis() - this.lastTimeWrite) < 1000) {
                    /* Limit reached and slotTime not over yet */
                    synchronized (this) {
                        try {
                            this.wait(1000 - this.slotTimeLeft);
                        } catch (final InterruptedException e) {
                            throw new IOException("throttle interrupted");
                        }
                    }
                    /* refill Limit */
                    this.limitCounter = this.limitCurrent;
                    if (this.limitCounter <= 0) {
                        this.limitCounter = this.rest;
                    }
                } else if (this.slotTimeLeft > 1000) {
                    /* slotTime is over, refill Limit too */
                    this.limitCounter = this.limitCurrent;
                    if (this.limitCounter <= 0) {
                        this.limitCounter = this.rest;
                    }
                }
                this.todo = Math.min(this.limitCounter, this.rest);
                this.out.write(b, this.offset, this.todo);
                this.offset += this.todo;
                this.rest -= this.todo;
                this.transferedCounter += this.todo;
                this.limitCounter -= this.todo;
                this.lastTimeWrite = System.currentTimeMillis();
            }
        }
    }

    /**
     * WARNING: this function has a huge overhead
     */
    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);
        this.transferedCounter++;
        if (this.limitCurrent != 0) {
            /* a Limit is set */
            this.limitCounter--;
            this.slotTimeLeft = 0;
            if (this.limitCounter <= 0 && (this.slotTimeLeft = System.currentTimeMillis() - this.lastTimeWrite) < 1000) {
                /* Limit reached and slotTime not over yet */
                synchronized (this) {
                    try {
                        this.wait(1000 - this.slotTimeLeft);
                    } catch (final InterruptedException e) {
                        throw new IOException("throttle interrupted");
                    }
                }
                /* refill Limit */
                this.limitCounter = this.limitCurrent;
            } else if (this.slotTimeLeft > 1000) {
                /* slotTime is over, refill Limit too */
                this.limitCounter = this.limitCurrent;
            }
            this.lastTimeWrite = System.currentTimeMillis();
        }
    }

}
