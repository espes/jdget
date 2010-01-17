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

    private OutputStream out;
    private SpeedMeterInterface speedmeter = null;
    private long counted = 0;
    private long time = 0;
    private long speed = 0;

    /**
     * constructor for MeteredOutputStream
     * 
     * @param out
     */
    public MeteredOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * constructor for MeteredOutputStream with custom SpeedMeter
     * 
     * @param out
     * @param speedmeter
     */
    public MeteredOutputStream(OutputStream out, SpeedMeterInterface speedmeter) {
        this.out = out;
        this.speedmeter = speedmeter;
    }

    @Override
    public void write(int b) throws IOException {
        write(b);
        counted++;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        counted += len;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#getSpeedMeter()
     */
    @Override
    public long getSpeedMeter() {
        if (time == 0) {
            time = System.currentTimeMillis();
            return 0;
        }
        if (System.currentTimeMillis() - time < 1000) {
            if (speedmeter != null) return speedmeter.getSpeedMeter();
            return speed;
        }
        try {
            if (speedmeter != null) {
                speedmeter.putSpeedMeter(counted, System.currentTimeMillis() - time);
                return speedmeter.getSpeedMeter();
            } else {
                speed = (counted / (System.currentTimeMillis() - time)) * 1000;
                return speed;
            }
        } finally {
            time = System.currentTimeMillis();
            counted = 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#putSpeedMeter(long, long)
     */
    @Override
    public void putSpeedMeter(long bytes, long time) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.SpeedMeterInterface#resetSpeedMeter()
     */
    @Override
    public void resetSpeedMeter() {
        if (speedmeter != null) {
            speedmeter.resetSpeedMeter();
        } else {
            counted = 0;
        }
    }

}
