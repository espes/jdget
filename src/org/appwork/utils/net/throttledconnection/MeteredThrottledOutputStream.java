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

import java.io.OutputStream;

import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class MeteredThrottledOutputStream extends ThrottledOutputStream implements SpeedMeterInterface {

    private SpeedMeterInterface speedmeter = null;
    private long time = 0;
    private long speed = 0;
    private long lastTime;
    private long lastTrans;
    private long transferedCounter3;
    private final Object LOCK = new Object();

    /**
     * @param out
     */
    public MeteredThrottledOutputStream(OutputStream out) {
        super(out);
    }

    public MeteredThrottledOutputStream(OutputStream out, SpeedMeterInterface speedmeter) {
        super(out);
        this.speedmeter = speedmeter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#getSpeedMeter()
     */

    public long getSpeedMeter() {
        synchronized (LOCK) {
            if (time == 0) {
                time = System.currentTimeMillis();
                transferedCounter3 = transferedCounter;
                return 0;
            }
            if (System.currentTimeMillis() - time < 1000) {
                if (speedmeter != null) return speedmeter.getSpeedMeter();
                return speed;
            }
            lastTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            lastTrans = transferedCounter - transferedCounter3;
            transferedCounter3 = transferedCounter;
            if (speedmeter != null) {
                speedmeter.putSpeedMeter(lastTrans, lastTime);
                speed = speedmeter.getSpeedMeter();
                return speed;
            } else {
                speed = (lastTrans / lastTime) * 1000;
                return speed;
            }
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

    public void resetSpeedMeter() {
        synchronized (LOCK) {
            if (speedmeter != null) speedmeter.resetSpeedMeter();
            time = System.currentTimeMillis();
            speed = 0;
            transferedCounter3 = transferedCounter;
        }
    }

}
