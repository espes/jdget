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

/**
 * @author daniel
 * 
 */
public class ThrottledConnectionManager {

    private ArrayList<ThrottledConnection> managedIn = new ArrayList<ThrottledConnection>();
    private ArrayList<ThrottledConnection> managedOut = new ArrayList<ThrottledConnection>();
    private final Object LOCK = new Object();
    /**
     * how fast do we want to update and check current status
     */
    private final static int updateSpeed = 2000;

    private Thread watchDog = null;

    private long IncommingBandwidthLimit = 0;
    private long IncommingBandwidthUsage = 0;

    private long OutgoingBandwidthLimit = 0;
    private long OutgoingBandwidthUsage = 0;

    public ThrottledConnectionManager() {
    }

    /**
     * returns a managed ThrottledInputStream for given InputStream
     * 
     * @param in
     * @return
     */
    public ThrottledInputStream getManagedThrottledInputStream(InputStream in) {
        ThrottledInputStream ret = new ThrottledInputStream(in, this);
        addManagedThrottledInputStream(ret);
        return ret;
    }

    /**
     * returns a managed ThrottledOutputStream for given OutputStream
     * 
     * @param out
     * @return
     */
    public ThrottledOutputStream getManagedThrottledOutputStream(OutputStream out) {
        ThrottledOutputStream ret = new ThrottledOutputStream(out, this);
        addManagedThrottledOutputStream(ret);
        return ret;
    }

    /**
     * adds ThrottledInputStream to this manager
     * 
     * @param tin
     */
    public void addManagedThrottledInputStream(ThrottledInputStream tin) {
        synchronized (LOCK) {
            if (managedIn.contains(tin)) return;
            managedIn.add(tin);
            tin.setManager(this);
            tin.setManagedLimit(IncommingBandwidthLimit / managedIn.size());
            startWatchDog();
        }
    }

    /**
     * adds ThrottledOutputStream to this manager
     * 
     * @param tout
     */
    public void addManagedThrottledOutputStream(ThrottledOutputStream tout) {
        synchronized (LOCK) {
            if (managedOut.contains(tout)) return;
            managedOut.add(tout);
            tout.setManager(this);
            tout.setManagedLimit(OutgoingBandwidthLimit / managedOut.size());
            startWatchDog();
        }
    }

    /**
     * removes ThrottledInputStream from this manager
     * 
     * @param tin
     * @return
     */
    public boolean removeManagedThrottledInputStream(ThrottledInputStream tin) {
        synchronized (LOCK) {
            boolean ret = managedIn.remove(tin);
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
    public boolean removeManagedThrottledOutputStream(ThrottledOutputStream tout) {
        synchronized (LOCK) {
            boolean ret = managedOut.remove(tout);
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
    public void setIncommingBandwidthLimit(long kpsLimit) {
        IncommingBandwidthLimit = Math.max(0, kpsLimit);
    }

    /**
     * returns incomming bandwidth limit
     * 
     * @return
     */
    public long geIncommingBandwidthLimit() {
        return IncommingBandwidthLimit;
    }

    /**
     * set outgoing bandwidth limit
     * 
     * @param kpsLimit
     */
    public void setOutgoingBandwidthLimit(long kpsLimit) {
        OutgoingBandwidthLimit = Math.max(0, kpsLimit);
    }

    /**
     * returns outgoing bandwidth limit
     * 
     * @return
     */
    public long getOutgoingBandwidthLimit() {
        return OutgoingBandwidthLimit;
    }

    /**
     * returns current outgoing bandwidth
     * 
     * @return
     */
    public long getOutgoingBandwidthUsage() {
        return OutgoingBandwidthUsage;
    }

    /**
     * returns current incomming bandwidth
     * 
     * @return
     */
    public long getIncommingBandwidthUsage() {
        return IncommingBandwidthUsage;
    }

    private long manageConnections(ArrayList<ThrottledConnection> managed, long limit) {
        synchronized (LOCK) {
            long currentManagedSpeed = 0;
            long managedConnections = 0;
            long currentRealSpeed = 0;
            for (ThrottledConnection in : managed) {
                long inspeed = in.transferedSinceLastCall();
                if (in.getCustomLimit() == 0) {
                    /* this connection is managed */
                    currentManagedSpeed += inspeed;
                    /*
                     * dont count connections with no bandwidth usage, eg lost
                     * ones
                     */
                    if (inspeed != 0) managedConnections++;
                }
                currentRealSpeed += inspeed;
            }
            /*
             * calculate new input limit based on current input bandwidth usage
             */
            currentManagedSpeed = (currentManagedSpeed / Math.max(1000, updateSpeed)) * 1000;
            currentRealSpeed = (currentRealSpeed / Math.max(1000, updateSpeed)) * 1000;
            long difference = currentManagedSpeed - limit;
            long newLimit = 0;
            if (managedConnections == 0) {
                newLimit = limit;
            } else if (difference >= 0) {
                /* faster than we wanted */
                newLimit = limit / managedConnections;
            } else {
                /* slower than we wanted */
                newLimit = (-difference + limit) / managedConnections;
            }
            for (ThrottledConnection in : managed) {
                if (limit == 0) {
                    /* we do not have a limit set */
                    in.setManagedLimit(0);
                } else {
                    /* set new limit */
                    in.setManagedLimit(newLimit);
                }
            }
            return currentRealSpeed;
        }
    }

    private synchronized void startWatchDog() {
        if (watchDog != null) return;
        watchDog = new Thread() {
            @Override
            public void run() {
                setName("ThrottlecConnectionManager");
                while (true) {
                    try {
                        sleep(Math.max(1000, updateSpeed));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IncommingBandwidthUsage = manageConnections(managedIn, IncommingBandwidthLimit);
                    OutgoingBandwidthUsage = manageConnections(managedOut, OutgoingBandwidthLimit);
                }
            }
        };
        watchDog.start();
    }
}
