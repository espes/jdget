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
import java.io.InputStream;

/**
 * @author daniel
 * 
 */
public class ThrottledInputStream extends InputStream implements ThrottledConnection {

    private ThrottledConnectionHandler handler;
    private final InputStream          in;
    protected volatile long            transferedCounter  = 0;
    protected volatile long            transferedCounter2 = 0;
    private volatile int               limitCurrent       = 0;
    private int                        limitCounter       = 0;
    private int                        lastRead2;

    private long                       slotTimeLeft       = 0;
    private long                       lastTimeRead       = 0;

    /**
     * constructor for not managed ThrottledInputStream
     * 
     * @param in
     */
    public ThrottledInputStream(final InputStream in) {
        this.in = in;
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    /**
     * DO NOT FORGET TO CLOSE
     */
    @Override
    public void close() throws IOException {
        /* remove this stream from handler */
        if (this.handler != null) {
            this.handler.removeThrottledConnection(this);
            this.handler = null;
        }
        synchronized (this) {
            this.notify();
        }
        this.in.close();
    }

    @Override
    public ThrottledConnectionHandler getHandler() {
        return this.handler;
    }

    @Override
    public int getLimit() {
        return this.limitCurrent;
    }

    @Override
    public synchronized void mark(final int readlimit) {
        this.in.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    /**
     * WARNING: this function has a huge overhead
     */
    @Override
    public int read() throws IOException {
        this.lastRead2 = this.in.read();
        if (this.lastRead2 == -1) {
            /* end of line */
            return -1;
        }
        this.transferedCounter++;
        if (this.limitCurrent != 0) {
            /* a Limit is set */
            this.limitCounter--;
            this.slotTimeLeft = 0;
            if (this.limitCounter <= 0 && (this.slotTimeLeft = System.currentTimeMillis() - this.lastTimeRead) < 1000) {
                /* Limit reached and slotTime not over yet */
                synchronized (this) {
                    try {
                        this.wait(1000 - this.slotTimeLeft);
                    } catch (final InterruptedException e) {
                        throw new IOException("throttle interrupted", e);
                    }
                }
                /* refill Limit */
                this.limitCounter = this.limitCurrent;
            } else if (this.slotTimeLeft > 1000) {
                /* slotTime is over, refill Limit too */
                this.limitCounter = this.limitCurrent;
            }
            this.lastTimeRead = System.currentTimeMillis();
        }
        return this.lastRead2;
    }

    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        if (this.limitCurrent == 0) {
            this.lastRead2 = this.in.read(b, off, len);
            if (this.lastRead2 == -1) {
                /* end of line */
                return -1;
            }
            this.transferedCounter += this.lastRead2;
        } else {
            /* a Limit is set */
            this.slotTimeLeft = 0;
            if (this.limitCounter <= 0 && (this.slotTimeLeft = System.currentTimeMillis() - this.lastTimeRead) < 1000) {
                /* Limit reached and slotTime not over yet */
                synchronized (this) {
                    try {
                        this.wait(1000 - this.slotTimeLeft);
                    } catch (final InterruptedException e) {
                        throw new IOException("throttle interrupted", e);
                    }
                }
                /* refill Limit */
                this.limitCounter = this.limitCurrent;
                if (this.limitCounter <= 0) {
                    this.limitCounter = len;
                }
            } else if (this.slotTimeLeft > 1000) {
                /* slotTime is over, refill Limit too */
                this.limitCounter = this.limitCurrent;
                if (this.limitCounter <= 0) {
                    this.limitCounter = len;
                }
            }
            this.lastRead2 = this.in.read(b, off, Math.min(this.limitCounter, len));
            if (this.lastRead2 == -1) {
                /* end of line */
                return -1;
            }
            this.transferedCounter += this.lastRead2;
            this.limitCounter -= this.lastRead2;
            this.lastTimeRead = System.currentTimeMillis();
        }
        return this.lastRead2;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
    }

    /**
     * set a new ThrottledConnectionHandler
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
    public long skip(final long n) throws IOException {
        return this.in.skip(n);
    }

    public long transfered() {
        return this.transferedCounter;
    }

}