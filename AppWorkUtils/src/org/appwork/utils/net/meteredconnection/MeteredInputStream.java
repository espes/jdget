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
import java.io.InputStream;

import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class MeteredInputStream extends InputStream implements SpeedMeterInterface {

    private InputStream in;
    private SpeedMeterInterface speedmeter = null;
    private long transfered = 0;
    public long getTransfered() {
        return transfered;
    }

    private long transfered2 = 0;
    private long time = 0;
    private int readTmp1;
    private long speed = 0;
    private int offset;
    private int checkStep = 1024;
    // private final static int HIGHStep = 524288;
    public final static int LOWStep = 1024;
    private int todo;
    private int lastRead;
    private int rest;
    private int lastRead2;
    private long lastTime;
    private long lastTrans;
    private long timeForCheckStep = 0;
    private int timeCheck = 0;

    /**
     * constructor for MeterdInputStream
     * 
     * @param in
     */
    public MeteredInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * constructor for MeteredInputStream with custom SpeedMeter
     * 
     * @param in
     * @param speedmeter
     */
    public MeteredInputStream(InputStream in, SpeedMeterInterface speedmeter) {
        this.in = in;
        this.speedmeter = speedmeter;
    }

    @Override
    public int read() throws IOException {
        readTmp1 = in.read();
        if (readTmp1 != -1) transfered++;
        return readTmp1;
    }

    public int getCheckStepSize() {
        return checkStep;
    }

    public void setCheckStepSize(int step) {
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
            timeForCheckStep = System.currentTimeMillis();
            lastRead = in.read(b, offset, todo);
            timeCheck = (int) (System.currentTimeMillis() - timeForCheckStep);
            if (lastRead == -1) break;
            if (timeCheck > 1000) {
                /* we want 5 update per second */
                checkStep = Math.max(LOWStep, (todo / timeCheck) * 500);
            } else if (timeCheck == 0) {
                /* we increase in little steps */
                checkStep += 1024;
                // checkStep = Math.min(HIGHStep, checkStep + 1024);
            }
            lastRead2 += lastRead;
            transfered += lastRead;
            rest -= lastRead;
            offset += lastRead;
        }
        if (lastRead == -1 && lastRead2 == 0) {
            return -1;
        } else {
            return lastRead2;
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

    @Override
    public void close() throws IOException {
        in.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#getSpeedMeter()
     */

    public synchronized long getSpeedMeter() {
        if (time == 0) {
            time = System.currentTimeMillis();
            transfered2 = transfered;
            return 0;
        }
        if (System.currentTimeMillis() - time < 1000) {
            if (speedmeter != null) return speedmeter.getSpeedMeter();
            return speed;
        }
        lastTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        lastTrans = transfered - transfered2;
        transfered2 = transfered;
        if (speedmeter != null) {
            speedmeter.putSpeedMeter(lastTrans, lastTime);
            speed = speedmeter.getSpeedMeter();
            return speed;
        } else {
            speed = (lastTrans / lastTime) * 1000;
            return speed;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#putSpeedMeter(long, long)
     */

    public void putSpeedMeter(long bytes, long time) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#resetSpeedMeter()
     */

    public synchronized void resetSpeedMeter() {
        if (speedmeter != null) speedmeter.resetSpeedMeter();
        speed = 0;
        transfered2 = transfered;
        time = System.currentTimeMillis();
    }
}
