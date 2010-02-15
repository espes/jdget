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

    private long[] bytes;
    private long[] times;
    private int size;
    private int index;
    private boolean changed = false;
    private long speed = 0;
    private final Object LOCK = new Object();
    private long stalled = 0;

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
    public AverageSpeedMeter(int size) {
        this.size = size;
        bytes = new long[this.size];
        times = new long[this.size];
        index = 0;
        resetSpeedMeter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#getSpeedMeter()
     */

    public long getSpeedMeter() {
        synchronized (LOCK) {
            if (!changed) return speed;
            long totalValue = 0;
            long totalTime = stalled;
            for (int i = 0; i < size; i++) {
                if (bytes[i] < 0) continue;
                totalValue += bytes[i];
                totalTime += times[i];
            }
            if (totalTime >= 1000) speed = ((totalValue * 1000) / totalTime);
            changed = false;
            return speed;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#putSpeedMeter(long,
     * long)
     */

    public void putSpeedMeter(long x, long time) {
        synchronized (LOCK) {
            long put = Math.max(0, x);
            if (put == 0) {
                stalled += Math.max(0, time);
            } else {
                bytes[index] = put;
                times[index] = Math.max(0, time) + stalled;
                stalled = 0;
                index++;
                if (index == size) index = 0;
            }
            changed = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#resetSpeedMeter()
     */

    public void resetSpeedMeter() {
        synchronized (LOCK) {
            for (index = 0; index < size; index++) {
                bytes[index] = -1;
                times[index] = 0;
            }
            index = 0;
            speed = 0;
            changed = true;
        }
    }

}
