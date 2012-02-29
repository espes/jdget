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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.appwork.utils.speedmeter.AverageSpeedMeter;
import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class ThrottledConnectionManager {

    private static class BalanceSpeed {
        protected long lastManaged    = 0;
        protected long lastDifference = 0;
        protected long lastSpeed      = 0;
    }

    private final ArrayList<ThrottledConnection> managedIn               = new ArrayList<ThrottledConnection>();
    private final ArrayList<ThrottledConnection> managedOut              = new ArrayList<ThrottledConnection>();
    private final Object                         LOCK                    = new Object();
    /**
     * how fast do we want to update and check current status
     */
    private final static int                     updateSpeed             = 2000;
    private final static int                     balanceStep             = 1024 * 25;

    private Thread                               watchDog                = null;

    private volatile int                         IncommingBandwidthLimit = 0;
    private volatile int                         IncommingBandwidthUsage = 0;
    private volatile long                        IncommingTraffic        = 0;
    private SpeedMeterInterface                  IncommingSpeedMeter     = null;

    private volatile int                         OutgoingBandwidthLimit  = 0;
    private volatile int                         OutgoingBandwidthUsage  = 0;
    private volatile long                        OutgoingTraffic         = 0;
    private SpeedMeterInterface                  OutgoingSpeedMeter      = null;

    public ThrottledConnectionManager() {
        this.IncommingSpeedMeter = new AverageSpeedMeter(10);
        this.OutgoingSpeedMeter = new AverageSpeedMeter(10);
    }

    /**
     * adds ThrottledConnection for Input to this manager
     * 
     * @param tin
     */
    public void addManagedThrottledInputConnection(final ThrottledConnection tcon) {
        synchronized (this.LOCK) {
            if (this.managedIn.contains(tcon)) { return; }
            this.managedIn.add(tcon);
            tcon.setManager(this);
            tcon.setManagedLimit(this.IncommingBandwidthLimit / this.managedIn.size());
            this.startWatchDog();
        }
    }

    /**
     * adds ThrottledInputStream to this manager
     * 
     * @param tin
     */
    public void addManagedThrottledInputStream(final ThrottledInputStream tin) {
        synchronized (this.LOCK) {
            if (this.managedIn.contains(tin)) { return; }
            this.managedIn.add(tin);
            tin.setManager(this);
            tin.setManagedLimit(this.IncommingBandwidthLimit / this.managedIn.size());
            this.startWatchDog();
        }
    }

    /**
     * adds ThrottledOutputStream to this manager
     * 
     * @param tout
     */
    public void addManagedThrottledOutputStream(final ThrottledOutputStream tout) {
        synchronized (this.LOCK) {
            if (this.managedOut.contains(tout)) { return; }
            this.managedOut.add(tout);
            tout.setManager(this);
            tout.setManagedLimit(this.OutgoingBandwidthLimit / this.managedOut.size());
            this.startWatchDog();
        }
    }

    /**
     * returns incomming bandwidth limit
     * 
     * @return
     */
    public int getIncommingBandwidthLimit() {
        return this.IncommingBandwidthLimit;
    }

    /**
     * returns current incomming bandwidth
     * 
     * @return
     */
    public int getIncommingBandwidthUsage() {
        return this.IncommingBandwidthUsage;
    }

    public int getIncommingConnections() {
        return this.managedIn.size();
    }

    public SpeedMeterInterface getIncommingSpeedMeter() {
        return this.IncommingSpeedMeter;
    }

    public long getIncommingTraffic() {
        return this.IncommingTraffic;
    }

    /**
     * returns a managed ThrottledInputStream for given InputStream
     * 
     * @param in
     * @return
     */
    public ThrottledInputStream getManagedThrottledInputStream(final InputStream in) {
        final ThrottledInputStream ret = new ThrottledInputStream(in, this);
        this.addManagedThrottledInputStream(ret);
        return ret;
    }

    /**
     * returns a managed ThrottledOutputStream for given OutputStream
     * 
     * @param out
     * @return
     */
    public ThrottledOutputStream getManagedThrottledOutputStream(final OutputStream out) {
        final ThrottledOutputStream ret = new ThrottledOutputStream(out, this);
        this.addManagedThrottledOutputStream(ret);
        return ret;
    }

    /**
     * returns outgoing bandwidth limit
     * 
     * @return
     */
    public int getOutgoingBandwidthLimit() {
        return this.OutgoingBandwidthLimit;
    }

    /**
     * returns current outgoing bandwidth
     * 
     * @return
     */
    public int getOutgoingBandwidthUsage() {
        return this.OutgoingBandwidthUsage;
    }

    public int getOutgoingConnections() {
        return this.managedOut.size();
    }

    public SpeedMeterInterface getOutgoingSpeedMeter() {
        return this.OutgoingSpeedMeter;
    }

    public long getOutgoingTraffic() {
        return this.OutgoingTraffic;
    }

    private int manageConnections(final ArrayList<ThrottledConnection> managed, int limit) {
        synchronized (this.LOCK) {
            int managedConnections = 0;
            int currentRealSpeed = 0;
            long ret;
            HashMap<ThrottledConnection, BalanceSpeed> difference = new HashMap<ThrottledConnection, BalanceSpeed>();
            for (final ThrottledConnection in : managed) {
                ret = in.transferedSinceLastCall();
                currentRealSpeed += ret;
                if (in.getCustomLimit() == 0) {
                    /* this connection is managed */
                    /*
                     * dont count connections with no bandwidth usage, eg lost
                     * ones
                     */
                    if (ret != 0) {
                        int lastSpeed = (int) ((long) ret * 1000 / Math.max(1000, ThrottledConnectionManager.updateSpeed));
                        BalanceSpeed bs = new BalanceSpeed();
                        bs.lastManaged = in.getManagedLimit();
                        bs.lastDifference = in.getManagedLimit() - lastSpeed;
                        bs.lastSpeed = lastSpeed;
                        difference.put(in, bs);
                        managedConnections++;
                    }
                }
            }
            /*
             * calculate new input limit based on current input bandwidth usage
             */
            currentRealSpeed = (int) ((long) currentRealSpeed * 1000 / Math.max(1000, ThrottledConnectionManager.updateSpeed));

            for (final ThrottledConnection in : managed) {
                if (managedConnections == 0) {
                    /* we do not have a limit set */
                    in.setManagedLimit(0);
                } else {
                    /* set new limit */
                    int newLimit = limit;
                    BalanceSpeed bs = difference.remove(in);
                    if (bs != null) {
                        /* this connection was managed before */
                        newLimit = limit / (difference.size() + 1);
                        if (bs.lastSpeed <= newLimit) {
                            /* last round was slower than minimum */
                            System.out.println("last round was slower");
                            int limit2 = Math.min((int) bs.lastManaged, newLimit);                            
                            if (bs.lastSpeed + balanceStep <= limit2) {
                                System.out.println("last round was slower: we can decrease minimum");
                                newLimit = limit2 - balanceStep;
                            } else {
                                if (bs.lastSpeed > newLimit) {
                                    System.out.println("speed is fine, no need to increase");
                                } else {
                                    System.out.println("last round was slower: increase maybe it can be faster");
                                    newLimit = limit2 + balanceStep;
                                }
                            }
                        } else {
                            newLimit = Math.min((int) bs.lastManaged, newLimit);
                        }
                        limit = limit - newLimit;
                    }
                    System.out.println(newLimit);
                    in.setManagedLimit(newLimit);
                }
            }
            return currentRealSpeed;
        }
    }

    /**
     * removes ThrottledConnection for Input from this manager
     * 
     * @param tin
     * @return
     */
    public boolean removeManagedThrottledInputConnection(final ThrottledConnection tcon) {
        synchronized (this.LOCK) {
            final boolean ret = this.managedIn.remove(tcon);
            if (ret) {
                tcon.setManager(null);
                tcon.setManagedLimit(0);
            }
            return ret;
        }
    }

    /**
     * removes ThrottledInputStream from this manager
     * 
     * @param tin
     * @return
     */
    public boolean removeManagedThrottledInputStream(final ThrottledInputStream tin) {
        synchronized (this.LOCK) {
            final boolean ret = this.managedIn.remove(tin);
            if (ret) {
                tin.setManager(null);
                tin.setManagedLimit(0);
            }
            return ret;
        }
    }

    /**
     * removes ThrottledOutputStream from this manager
     * 
     * @param tin
     * @return
     */
    public boolean removeManagedThrottledOutputStream(final ThrottledOutputStream tout) {
        synchronized (this.LOCK) {
            final boolean ret = this.managedOut.remove(tout);
            if (ret) {
                tout.setManager(null);
                tout.setManagedLimit(0);
            }
            return ret;
        }
    }

    /**
     * set incomming bandwidth limit
     * 
     * @param kpsLimit
     */
    public void setIncommingBandwidthLimit(final int kpsLimit) {
        this.IncommingBandwidthLimit = Math.max(0, kpsLimit);
    }

    /**
     * set outgoing bandwidth limit
     * 
     * @param kpsLimit
     */
    public void setOutgoingBandwidthLimit(final int kpsLimit) {
        this.OutgoingBandwidthLimit = Math.max(0, kpsLimit);
    }

    private synchronized void startWatchDog() {
        if (this.watchDog != null) { return; }
        this.watchDog = new Thread() {
            @Override
            public void run() {
                this.setName("ThrottlecConnectionManager");
                while (true) {
                    try {
                        Thread.sleep(Math.max(1000, ThrottledConnectionManager.updateSpeed));
                    } catch (final InterruptedException e) {
                        org.appwork.utils.logging.Log.exception(e);
                    }
                    ThrottledConnectionManager.this.IncommingBandwidthUsage = ThrottledConnectionManager.this.manageConnections(ThrottledConnectionManager.this.managedIn, ThrottledConnectionManager.this.IncommingBandwidthLimit);
                    if (ThrottledConnectionManager.this.managedIn.size() > 0) {
                        ThrottledConnectionManager.this.IncommingSpeedMeter.putSpeedMeter(ThrottledConnectionManager.this.IncommingBandwidthUsage, 1000);
                    }
                    ThrottledConnectionManager.this.IncommingTraffic += ThrottledConnectionManager.this.IncommingBandwidthUsage;
                    ThrottledConnectionManager.this.OutgoingBandwidthUsage = ThrottledConnectionManager.this.manageConnections(ThrottledConnectionManager.this.managedOut, ThrottledConnectionManager.this.OutgoingBandwidthLimit);
                    if (ThrottledConnectionManager.this.managedOut.size() > 0) {
                        ThrottledConnectionManager.this.OutgoingSpeedMeter.putSpeedMeter(ThrottledConnectionManager.this.OutgoingBandwidthUsage, 1000);
                    }
                    ThrottledConnectionManager.this.OutgoingTraffic += ThrottledConnectionManager.this.OutgoingBandwidthUsage;
                }
            }
        };
        this.watchDog.start();
    }
}
