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

    /**
     * constructor for AverageSpeedMeter with default size 10
     */
    public AverageSpeedMeter() {
        this(10);
    }

    /**
     * constructor for AverageSpeedMeter with custom size
     * 
     * @param size
     */
    public AverageSpeedMeter(int size) {
        bytes = new long[size];
        times = new long[size];
        index = 0;
        this.size = size;
        resetSpeedMeter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#getSpeedMeter()
     */
    @Override
    public long getSpeedMeter() {
        synchronized (LOCK) {
            if (!changed) return speed;
            long totalValue = 0;
            long totalTime = 0;
            for (int i = 0; i < size; i++) {
                if (bytes[i] < 0) continue;
                totalValue += bytes[i];
                totalTime += times[i];
            }
            if (totalTime >= 1000) speed = (totalValue / totalTime) * 1000;
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
    @Override
    public void putSpeedMeter(long x, long time) {
        synchronized (LOCK) {
            bytes[index] = Math.max(0, x);
            times[index] = Math.max(0, time);
            index++;
            if (index == size) index = 0;
            changed = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.speedmeter.SpeedMeterInterface#resetSpeedMeter()
     */
    @Override
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
