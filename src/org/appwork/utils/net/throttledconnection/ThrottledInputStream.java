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

    private ThrottledConnectionManager manager;
    private InputStream in;
    protected long transferedCounter = 0;
    protected long transferedCounter2 = 0;
    private long limitCurrent = 0;
    private long limitManaged = 0;
    private long limitCustom = 0;
    private long limitCounter = 0;
    private long lastLimitReached = 0;
    private int lastRead;
    private int lastRead2;
    public final static int HIGHStep = 524288;
    public final static int LOWStep = 1024;
    private int checkStep = 10240;
    private int offset;
    private int todo;
    private int rest;
    private long ret;
    private long timeForCheckStep = 0;
    private int timeCheck = 0;

    /**
     * constructor for not managed ThrottledInputStream
     * 
     * @param in
     */
    public ThrottledInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * constructor for not managed ThrottledInputStream with given limit(see
     * setCustomLimit)
     * 
     * @param in
     * @param kpsLimit
     */
    public ThrottledInputStream(InputStream in, long kpsLimit) {
        this(in);
        setCustomLimit(kpsLimit);
    }

    /**
     * constructor for managed ThrottledInputStream
     * 
     * @param in
     * @param manager
     */
    protected ThrottledInputStream(InputStream in, ThrottledConnectionManager manager) {
        this.manager = manager;
        this.in = in;
    }

    public int getCheckStepSize() {
        return checkStep;
    }

    public void setCheckStepSize(int step) {
        checkStep = Math.max(HIGHStep, step);
        checkStep = Math.min(LOWStep, checkStep);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        offset = off;
        rest = len;
        lastRead2 = 0;
        while (rest != 0) {
            todo = rest;
            if (todo > checkStep) todo = checkStep;
            if (limitCurrent != 0) {
                lastRead = in.read(b, offset, todo);
            } else {
                timeForCheckStep = System.currentTimeMillis();
                lastRead = in.read(b, offset, todo);
                timeCheck = (int) (System.currentTimeMillis() - timeForCheckStep);
                if (timeCheck > 1000) {
                    /* we want 2 update per second */
                    checkStep = Math.max(LOWStep, (todo / timeCheck) * 500);
                } else if (timeCheck == 0) {
                    /* we increase in little steps */
                    checkStep += 1024;
                    // checkStep = Math.min(HIGHStep, checkStep + 1024);
                }
            }
            if (lastRead == -1) break;
            lastRead2 += lastRead;
            increase(lastRead);
            rest -= lastRead;
            offset += lastRead;
        }
        if (lastRead == -1 && lastRead2 == 0) {
            return -1;
        } else {
            return lastRead2;
        }
    }

    /**
     * WARNING: this function has a huge overhead
     */
    @Override
    public int read() throws IOException {
        lastRead2 = in.read();
        increase(lastRead2);
        return lastRead2;
    }

    final private void increase(int num) {
        if (num == -1) return;
        transferedCounter += num;
        if (limitCurrent != 0) {
            limitCounter += num;
            if (limitCounter > limitCurrent) {
                if (lastLimitReached == 0) {
                    /* our first read */
                    lastLimitReached = System.currentTimeMillis();
                }
                /* we set limit in kbyte per second */
                long pause = 1000 - (System.currentTimeMillis() - lastLimitReached);
                if (pause >= 0) {
                    if (pause == 0) pause = 1000;
                    synchronized (this) {
                        try {
                            wait(pause);
                        } catch (InterruptedException e) {
                            org.appwork.utils.logging.Log.exception(e);
                        }
                    }
                    /* change checkStep according to limit */
                    if (limitCurrent >= HIGHStep) {
                        checkStep = HIGHStep + 1;
                    } else if (limitCurrent <= LOWStep) {
                        checkStep = LOWStep;
                    } else {
                        checkStep = (int) limitCurrent + 1;
                    }
                }
                lastLimitReached = System.currentTimeMillis();
                limitCounter = 0;
            }
        } else {
            /* increase step size up to HIGHStep limit */
            checkStep = HIGHStep;
        }
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * return how many bytes got transfered till now and reset counter
     * 
     * @return
     */
    public synchronized long transferedSinceLastCall() {
        ret = transferedCounter - transferedCounter2;
        transferedCounter2 = transferedCounter;
        return ret;
    }

    /**
     * DO NOT FORGET TO CLOSE
     */
    @Override
    public void close() throws IOException {
        /* remove this stream from manager */
        if (manager != null) {
            manager.removeManagedThrottledInputStream(this);
            manager = null;
        }
        synchronized (this) {
            notify();
        }
        in.close();
    }

    /**
     * set a new ThrottledConnectionManager
     * 
     * @param manager
     */
    public void setManager(ThrottledConnectionManager manager) {
        if (this.manager != null && this.manager != manager) this.manager.removeManagedThrottledInputStream(this);
        this.manager = manager;
        if (this.manager != null) this.manager.addManagedThrottledInputStream(this);
    }

    /**
     * sets managed limit 0: no limit >0: use managed limit
     * 
     * @param kpsLimit
     */
    public void setManagedLimit(long kpsLimit) {
        if (kpsLimit == limitManaged) return;
        if (kpsLimit <= 0) {
            limitManaged = 0;
            if (limitCustom == 0) changeCurrentLimit(0);
        } else {
            limitManaged = kpsLimit;
            if (limitCustom == 0) changeCurrentLimit(kpsLimit);
        }
    }

    /**
     * sets custom speed limit -1 : no limit 0 : use managed limit >0: use
     * custom limit
     * 
     * @param kpsLimit
     */
    public void setCustomLimit(long kpsLimit) {
        if (limitCustom == kpsLimit) return;
        if (kpsLimit < 0) {
            limitCustom = -1;
            changeCurrentLimit(0);
        } else if (kpsLimit == 0) {
            limitCustom = 0;
            changeCurrentLimit(limitManaged);
        } else {
            limitCustom = kpsLimit;
            changeCurrentLimit(kpsLimit);
        }
    }

    /**
     * get custom set limit
     * 
     * @return
     */
    public long getCustomLimit() {
        return limitCustom;
    }

    /**
     * change current limit
     * 
     * @param kpsLimit
     */
    private void changeCurrentLimit(long kpsLimit) {
        if (kpsLimit == limitCurrent) return;
        /* TODO: maybe allow little jitter here */
        limitCurrent = Math.max(0, kpsLimit);
        synchronized (this) {
            notify();
        }
    }
}