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
import java.net.MalformedURLException;

import org.appwork.utils.net.NullOutputStream;
import org.appwork.utils.speedmeter.AverageSpeedMeter;

/**
 * @author daniel
 * 
 */
public class ThrottledOutputStream extends OutputStream implements ThrottledConnection {

    /**
     * Tester
     * 
     * @param args
     * @throws MalformedURLException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws IOException, InterruptedException {
        final MeteredThrottledOutputStream os = new MeteredThrottledOutputStream(new NullOutputStream(), new AverageSpeedMeter(5));
        os.setLimit(99 * 1024);
        final byte[] buffer = new byte[18 * 1024];
        while (true) {
            os.write(buffer);
            final long speed = os.getSpeedMeter();
            System.out.println("speed is " + speed + " limit is " + os.getLimit() + " difference " + (os.getLimit() - speed));
        }
    }

    private ThrottledConnectionHandler handler;

    private OutputStream               out;
    protected volatile long            transferedCounter  = 0;
    protected volatile long            transferedCounter2 = 0;
    private volatile int               limitCurrent       = 0;

    private int                        limitCounter       = 0;
    private int                        offset;
    private int                        todo;
    private int                        rest;
    private long                       slotTimeLeft       = 0;

    private long                       lastTimeReset      = 0;
    private final long                 onems              = 1000000l;
    private final long                 onesec             = 1000000000l;

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

    public OutputStream getOutputStream() {
        return this.out;
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

    public void setOutputStream(final OutputStream os) {
        if (os == null) { throw new IllegalArgumentException("Outputstream is null"); }
        if (os == this) { throw new IllegalArgumentException("Outputstream loop!"); }
        this.out = os;
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
                this.writeWait(this.rest);
                this.todo = Math.min(this.limitCounter, this.rest);
                this.out.write(b, this.offset, this.todo);
                this.offset += this.todo;
                this.rest -= this.todo;
                this.transferedCounter += this.todo;
                this.limitCounter -= this.todo;
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
            this.writeWait(1);
        }
    }

    private final void writeWait(final int len) throws IOException {
        /* a Limit is set */
        final long current = System.nanoTime();
        this.slotTimeLeft = Math.max(0, current - this.lastTimeReset);
        if (this.limitCounter <= 0 && this.slotTimeLeft < this.onesec) {
            /* Limit reached and slotTime not over yet */
            synchronized (this) {
                try {
                    long wait = this.onesec - this.slotTimeLeft;
                    this.lastTimeReset = current + wait;
                    final long ns = wait % this.onems;
                    wait = wait / this.onems;
                    this.wait(wait, (int) ns);
                } catch (final InterruptedException e) {
                    throw new IOException("throttle interrupted", e);
                }
            }
            /* refill Limit */
            this.limitCounter = this.limitCurrent;
            if (this.limitCounter <= 0) {
                this.limitCounter = len;
            }
        } else if (this.slotTimeLeft >= this.onesec) {
            /* slotTime is over, refill Limit too */
            this.limitCounter = this.limitCurrent;
            this.lastTimeReset = current;
            if (this.limitCounter <= 0) {
                this.limitCounter = len;
            }
        }
    }

}
