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
    private long transfered = 0;
    private long transfered2 = 0;
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
        transfered++;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        transfered += len;
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
        try {
            if (speedmeter != null) {
                speedmeter.putSpeedMeter(transfered - transfered2, System.currentTimeMillis() - time);
                return speedmeter.getSpeedMeter();
            } else {
                speed = ((transfered - transfered2) / (System.currentTimeMillis() - time)) * 1000;
                return speed;
            }
        } finally {
            transfered2 = transfered;
            time = System.currentTimeMillis();
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
    public synchronized void resetSpeedMeter() {
        if (speedmeter != null) {
            speedmeter.resetSpeedMeter();
        } else {
            transfered2 = transfered = 0;
        }
    }

}
