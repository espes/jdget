/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.meteredconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.meteredconnection;

import java.io.IOException;
import java.io.OutputStream;

import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class MeteredOutputStream extends OutputStream implements SpeedMeterInterface {

    private final OutputStream  out;
    private SpeedMeterInterface speedmeter       = null;
    private long                transfered       = 0;
    private long                transfered2      = 0;
    private long                time             = 0;
    private long                speed            = 0;
    private int                 offset;
    private int                 checkStep        = 1024;
    // private final static int HIGHStep = 524288;
    public final static int     LOWStep          = 1024;
    private int                 rest;
    private int                 todo;
    private long                lastTime;
    private long                lastTrans;
    private long                timeForCheckStep = 0;
    private int                 timeCheck        = 0;

    /**
     * constructor for MeteredOutputStream
     * 
     * @param out
     */
    public MeteredOutputStream(final OutputStream out) {
        this.out = out;
    }

    /**
     * constructor for MeteredOutputStream with custom SpeedMeter
     * 
     * @param out
     * @param speedmeter
     */
    public MeteredOutputStream(final OutputStream out, final SpeedMeterInterface speedmeter) {
        this.out = out;
        this.speedmeter = speedmeter;
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    public int getCheckStepSize() {
        return this.checkStep;
    }

    public synchronized long getSpeedMeter() {
        if (this.time == 0) {
            this.time = System.currentTimeMillis();
            this.transfered2 = this.transfered;
            return 0;
        }
        if (System.currentTimeMillis() - this.time < 1000) {
            if (this.speedmeter != null) { return this.speedmeter.getSpeedMeter(); }
            return this.speed;
        }
        this.lastTime = System.currentTimeMillis() - this.time;
        this.time = System.currentTimeMillis();
        this.lastTrans = this.transfered - this.transfered2;
        this.transfered2 = this.transfered;
        if (this.speedmeter != null) {
            this.speedmeter.putSpeedMeter(this.lastTrans, this.lastTime);
            return this.speedmeter.getSpeedMeter();
        } else {
            this.speed = this.lastTrans / this.lastTime * 1000;
            return this.speed;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#putSpeedMeter(long, long)
     */
    public void putSpeedMeter(final long bytes, final long time) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#resetSpeedMeter()
     */
    public synchronized void resetSpeedMeter() {
        if (this.speedmeter != null) {
            this.speedmeter.resetSpeedMeter();
        }
        this.speed = 0;
        this.transfered2 = this.transfered;
        this.time = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#getSpeedMeter()
     */

    public void setCheckStepSize(final int step) {
        this.checkStep = Math.min(MeteredOutputStream.LOWStep, this.checkStep);
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
            this.timeForCheckStep = System.currentTimeMillis();
            this.out.write(b, this.offset, this.todo);
            this.timeCheck = (int) (System.currentTimeMillis() - this.timeForCheckStep);
            if (this.timeCheck > 1000) {
                /* we want 2 update per second */
                this.checkStep = Math.max(MeteredOutputStream.LOWStep, this.todo / this.timeCheck * 500);
            } else if (this.timeCheck == 0) {
                /* we increase in little steps */
                this.checkStep += 1024;
                // checkStep = Math.min(HIGHStep, checkStep + 1024);
            }
            this.transfered += this.todo;
            this.rest -= this.todo;
            this.offset += this.todo;
        }
    }

    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);
        this.transfered++;
    }

}
