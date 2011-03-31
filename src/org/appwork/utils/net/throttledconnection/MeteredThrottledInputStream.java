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

import java.io.InputStream;

import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class MeteredThrottledInputStream extends ThrottledInputStream implements SpeedMeterInterface {

    private SpeedMeterInterface speedmeter = null;
    private long                time       = 0;
    private long                speed      = 0;
    private long                lastTime;
    private long                lastTrans;
    private long                transferedCounter3;

    /**
     * @param in
     */
    public MeteredThrottledInputStream(final InputStream in) {
        super(in);
    }

    public MeteredThrottledInputStream(final InputStream in, final SpeedMeterInterface speedmeter) {
        super(in);
        this.speedmeter = speedmeter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#getSpeedMeter()
     */
    public synchronized long getSpeedMeter() {
        if (this.time == 0) {
            this.transferedCounter3 = this.transferedCounter;
            this.time = System.currentTimeMillis();
            return 0;
        }
        if (System.currentTimeMillis() - this.time < 1000) {
            if (this.speedmeter != null) { return this.speedmeter.getSpeedMeter(); }
            return this.speed;
        }
        final long tmp2 = this.transferedCounter;
        this.lastTrans = tmp2 - this.transferedCounter3;
        final long tmp = System.currentTimeMillis();
        this.lastTime = tmp - this.time;
        this.transferedCounter3 = tmp2;
        this.time = tmp;
        if (this.speedmeter != null) {
            this.speedmeter.putSpeedMeter(this.lastTrans, this.lastTime);
            this.speed = this.speedmeter.getSpeedMeter();
            return this.speed;
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
        this.time = System.currentTimeMillis();
        this.speed = 0;
        this.transferedCounter3 = this.transferedCounter;
    }
}