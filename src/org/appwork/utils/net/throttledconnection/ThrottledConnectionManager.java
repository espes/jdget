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

    private ArrayList<ThrottledInputStream> managedIn = new ArrayList<ThrottledInputStream>();
    private ArrayList<ThrottledOutputStream> managedOut = new ArrayList<ThrottledOutputStream>();
    private final Object LOCK = new Object();
    /**
     * how fast do we want to update and check current status
     */
    private final int updateSpeed = 2000;

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
            if (ret) tin.setManagedLimit(0);
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
            if (ret) tout.setManagedLimit(0);
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

    private void manageInputSpeed() {
        synchronized (LOCK) {
            long currentManagedInputSpeed = 0;
            long managedConnections = 0;
            long currentRealInputSpeed = 0;
            for (ThrottledInputStream in : managedIn) {
                long inspeed = in.resetTransferdCounted();
                if (in.getCustomLimit() == 0) {
                    /* this connection is managed */
                    currentManagedInputSpeed += inspeed;
                    managedConnections++;
                }
                currentRealInputSpeed += inspeed;
            }
            /*
             * calculate new input limit based on current input bandwidth usage
             */
            currentManagedInputSpeed = (currentManagedInputSpeed / updateSpeed) * 1000;
            IncommingBandwidthUsage = (currentRealInputSpeed / updateSpeed) * 1000;
            long difference = currentManagedInputSpeed - IncommingBandwidthLimit;
            long newLimit = 0;
            if (managedConnections == 0) {
                newLimit = IncommingBandwidthLimit;
            } else if (difference >= 0) {
                /* faster than we wanted */
                newLimit = IncommingBandwidthLimit / managedConnections;
            } else {
                /* slower than we wanted */
                newLimit = (-difference + IncommingBandwidthLimit) / managedConnections;
            }
            for (ThrottledInputStream in : managedIn) {
                if (IncommingBandwidthLimit == 0) {
                    /* we do not have a limit set */
                    in.setManagedLimit(0);
                } else {
                    /* set new limit */
                    in.setManagedLimit(newLimit);
                }
            }
        }
    }

    private void manageOutputSpeed() {
        synchronized (LOCK) {
            long currentManagedOutputSpeed = 0;
            long managedConnections = 0;
            long currentRealOutputSpeed = 0;
            for (ThrottledOutputStream out : managedOut) {
                long outspeed = out.resetTransferdCounted();
                if (out.getCustomLimit() == 0) {
                    /* this connection is managed */
                    currentManagedOutputSpeed += outspeed;
                    managedConnections++;
                }
                currentRealOutputSpeed += outspeed;
            }
            /*
             * calculate new output limit based on current output bandwidth
             * usage
             */
            currentManagedOutputSpeed = (currentManagedOutputSpeed / updateSpeed) * 1000;
            OutgoingBandwidthUsage = (currentRealOutputSpeed / updateSpeed) * 1000;
            long difference = currentManagedOutputSpeed - OutgoingBandwidthLimit;
            long newLimit = 0;
            if (managedConnections == 0) {
                newLimit = OutgoingBandwidthLimit;
            } else if (difference >= 0) {
                /* faster than we wanted */
                newLimit = OutgoingBandwidthLimit / managedConnections;
            } else {
                /* slower than we wanted */
                newLimit = (-difference + OutgoingBandwidthLimit) / managedConnections;
            }
            for (ThrottledOutputStream out : managedOut) {
                if (OutgoingBandwidthLimit == 0) {
                    /* we do not have a limit set */
                    out.setManagedLimit(0);
                } else {
                    /* set new limit */
                    out.setManagedLimit(newLimit);
                }
            }
        }
    }

    private synchronized void startWatchDog() {
        if (watchDog != null) return;
        watchDog = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(updateSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    manageInputSpeed();
                    manageOutputSpeed();
                }
            }
        };
        watchDog.start();
    }
}
