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
public interface SpeedMeterInterface {

    /**
     * resets the speed meter
     */
    public void resetSpeedMeter();

    /**
     * returns speed in byte/s
     * 
     * @return
     */
    public long getSpeedMeter();

    /**
     * put bytes/time into this speed meter
     * 
     * @param bytes
     * @param time
     */
    public void putSpeedMeter(long bytes, long time);
}
