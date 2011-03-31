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

    private ThrottledConnectionManager manager;
    private final OutputStream         out;

    protected long                     transferedCounter  = 0;
    protected long                     transferedCounter2 = 0;
    private long                       limitCurrent       = 0;
    private long                       limitManaged       = 0;
    private long                       limitCustom        = 0;
    private long                       limitCounter       = 0;
    private long                       lastLimitReached   = 0;
    public final static int            HIGHStep           = 524288;
    public final static int            LOWStep            = 1024;
    private int                        checkStep          = 10240;
    private int                        offset;
    private int                        todo;
    private int                        rest;
    private long                       ret;
    private long                       timeForCheckStep   = 0;
    private int                        timeCheck          = 0;

    /**
     * constructor for not managed ThrottledOutputStream
     * 
     * @param in
     */
    public ThrottledOutputStream(final OutputStream out) {
        this.out = out;
    }

    /**
     * constructor for not managed ThrottledOutputStream with given limit(see
     * setCustomLimit)
     * 
     * @param in
     * @param kpsLimit
     */
    public ThrottledOutputStream(final OutputStream out, final int kpsLimit) {
        this(out);
        this.setCustomLimit(kpsLimit);
    }

    /**
     * constructor for managed ThrottledOutputStream
     * 
     * @param in
     * @param manager
     */
    protected ThrottledOutputStream(final OutputStream out, final ThrottledConnectionManager manager) {
        this.manager = manager;
        this.out = out;
    }

    /**
     * change current limit
     * 
     * @param kpsLimit
     */
    private void changeCurrentLimit(final long kpsLimit) {
        if (kpsLimit == this.limitCurrent) { return; }
        /* TODO: maybe allow little jitter here */
        this.limitCurrent = Math.max(0, kpsLimit);
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * DO NOT FORGET TO CLOSE
     */
    @Override
    public void close() throws IOException {
        /* remove this stream from manager */
        if (this.manager != null) {
            this.manager.removeManagedThrottledOutputStream(this);
            this.manager = null;
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

    public int getCheckStepSize() {
        return this.checkStep;
    }

    /**
     * get custom set limit
     * 
     * @return
     */
    public long getCustomLimit() {
        return this.limitCustom;
    }

    final private void increase(final int num) {
        if (num == -1) { return; }
        this.transferedCounter += num;
        if (this.limitCurrent != 0) {
            this.limitCounter += num;
            if (this.limitCounter > this.limitCurrent) {
                if (this.lastLimitReached == 0) {
                    /* our first write */
                    this.lastLimitReached = System.currentTimeMillis();
                }
                /* we set limit in kbyte per second */
                long pause = 1000 - (System.currentTimeMillis() - this.lastLimitReached);
                if (pause >= 0) {
                    if (pause == 0) {
                        pause = 1000;
                    }
                    synchronized (this) {
                        try {
                            this.wait(pause);
                        } catch (final InterruptedException e) {
                            org.appwork.utils.logging.Log.exception(e);
                        }
                    }
                    /* change checkStep according to limit */
                    if (this.limitCurrent >= ThrottledOutputStream.HIGHStep) {
                        this.checkStep = ThrottledOutputStream.HIGHStep + 1;
                    } else if (this.limitCurrent <= ThrottledOutputStream.LOWStep) {
                        this.checkStep = ThrottledOutputStream.LOWStep;
                    } else {
                        this.checkStep = (int) this.limitCurrent + 1;
                    }
                }
                this.lastLimitReached = System.currentTimeMillis();
                this.limitCounter = 0;
            }
        }
    }

    public void setCheckStepSize(final int step) {
        this.checkStep = Math.max(ThrottledOutputStream.HIGHStep, step);
        this.checkStep = Math.min(ThrottledOutputStream.LOWStep, this.checkStep);
    }

    /**
     * sets custom speed limit -1 : no limit 0 : use managed limit >0: use
     * custom limit
     * 
     * @param kpsLimit
     */
    public void setCustomLimit(final int kpsLimit) {
        if (this.limitCustom == kpsLimit) { return; }
        if (kpsLimit < 0) {
            this.limitCustom = -1;
            this.changeCurrentLimit(0);
        } else if (kpsLimit == 0) {
            this.limitCustom = 0;
            this.changeCurrentLimit(this.limitManaged);
        } else {
            this.limitCustom = kpsLimit;
            this.changeCurrentLimit(kpsLimit);
        }
    }

    /**
     * sets managed limit 0: no limit >0: use managed limit
     * 
     * @param kpsLimit
     */
    public void setManagedLimit(final int kpsLimit) {
        if (kpsLimit == this.limitManaged) { return; }
        if (kpsLimit <= 0) {
            this.limitManaged = 0;
            if (this.limitCustom == 0) {
                this.changeCurrentLimit(0);
            }
        } else {
            this.limitManaged = kpsLimit;
            if (this.limitCustom == 0) {
                this.changeCurrentLimit(kpsLimit);
            }
        }
    }

    /**
     * set a new ThrottledConnectionManager
     * 
     * @param manager
     */
    public void setManager(final ThrottledConnectionManager manager) {
        if (this.manager != null && this.manager != manager) {
            this.manager.removeManagedThrottledOutputStream(this);
        }
        this.manager = manager;
        if (this.manager != null) {
            this.manager.addManagedThrottledOutputStream(this);
        }
    }

    /**
     * return how many bytes got transfered till now and reset counter
     * 
     * @return
     */
    public synchronized long transferedSinceLastCall() {
        this.ret = this.transferedCounter - this.transferedCounter2;
        this.transferedCounter2 = this.transferedCounter;
        return this.ret;
    }

    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        this.offset = off;
        this.rest = len;
        while (this.rest != 0) {
            this.todo = this.rest;
            if (this.todo > this.checkStep) {
                this.todo = this.checkStep;
            }
            if (this.limitCurrent != 0) {
                this.out.write(b, this.offset, this.todo);
            } else {
                this.timeForCheckStep = System.currentTimeMillis();
                this.out.write(b, this.offset, this.todo);
                this.timeCheck = (int) (System.currentTimeMillis() - this.timeForCheckStep);
                if (this.timeCheck > 1000) {
                    /* we want more than2 update per second */
                    this.checkStep = Math.max(ThrottledOutputStream.LOWStep, this.todo / this.timeCheck * 500);
                } else if (this.timeCheck == 0) {
                    /* we increase in little steps */
                    this.checkStep += 1024;
                    // checkStep = Math.min(HIGHStep, checkStep + 1024);
                }
            }
            this.increase(this.todo);
            this.rest -= this.todo;
            this.offset += this.todo;
        }
    }

    /**
     * WARNING: this function has a huge overhead
     */
    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);
        this.increase(1);
    }

}
