/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.speedmeter
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.speedmeter;

/**
 * @author daniel
 * 
 */
public class AverageSpeedMeter implements SpeedMeterInterface {

    private final long[] bytes;
    private final long[] times;
    private final int    size;
    private int          index;
    private boolean      changed = false;
    private long         speed   = 0;
    private final Object LOCK    = new Object();
    private long         stalled = 0;
    private long         timeout = -1;          /*
                                                  * no timeout for stalled
                                                  * connections
                                                  */

    /**
     * constructor for AverageSpeedMeter with default size 5
     */
    public AverageSpeedMeter() {
        this(5);
    }

    /**
     * constructor for AverageSpeedMeter with custom size
     * 
     * @param size
     */
    public AverageSpeedMeter(final int size) {
        this.size = size;
        this.bytes = new long[this.size];
        this.times = new long[this.size];
        this.index = 0;
        this.resetSpeedMeter();
    }

    public long getSpeedMeter() {
        synchronized (this.LOCK) {
            if (!this.changed) { return this.speed; }
            long totalValue = 0;
            long totalTime = this.stalled;
            for (int i = 0; i < this.size; i++) {
                if (this.bytes[i] < 0) {
                    continue;
                }
                totalValue += this.bytes[i];
                totalTime += this.times[i];
            }
            if (totalTime >= 1000) {
                this.speed = totalValue * 1000 / totalTime;
            }
            this.changed = false;
            return this.speed;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#getSpeedMeter()
     */

    public void putSpeedMeter(final long x, final long time) {
        synchronized (this.LOCK) {
            final long put = Math.max(0, x);
            if (put == 0) {
                this.stalled += Math.max(0, time);
                if (this.timeout > 0 && this.stalled > this.timeout) {
                    this.resetSpeedMeter();
                }
            } else {
                this.bytes[this.index] = put;
                this.times[this.index] = Math.max(0, time) + this.stalled;
                this.stalled = 0;
                this.index++;
                if (this.index == this.size) {
                    this.index = 0;
                }
            }
            this.changed = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#putSpeedMeter(long,
     * long)
     */

    public void resetSpeedMeter() {
        synchronized (this.LOCK) {
            for (this.index = 0; this.index < this.size; this.index++) {
                this.bytes[this.index] = -1;
                this.times[this.index] = 0;
            }
            this.index = 0;
            this.speed = 0;
            this.changed = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#resetSpeedMeter()
     */

    public void setStallTimeout(final long timeout) {
        if (timeout <= 0) {
            this.timeout = -1;
        } else {
            this.timeout = timeout;
        }
    }

}
