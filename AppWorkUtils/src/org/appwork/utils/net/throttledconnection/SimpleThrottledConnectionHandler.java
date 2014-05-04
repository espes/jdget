/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.throttledconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.throttledconnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appwork.utils.logging.Log;
import org.appwork.utils.speedmeter.AverageSpeedMeter;
import org.appwork.utils.speedmeter.SpeedMeterInterface;

/**
 * @author daniel
 * 
 */
public class SimpleThrottledConnectionHandler implements ThrottledConnectionHandler {

    private static class SpeedAssignHelp {
        protected long lastLimit      = 0;
        protected int  newLimit       = 0;
        protected long lastDifference = 0;
        protected long lastSpeed      = 0;
        protected long lastTraffic    = 0;
        protected long lastTimeStamp  = 0;
    }

    protected Thread                         watchDog     = null;
    protected java.util.List<ThrottledConnection> connections  = new ArrayList<ThrottledConnection>();
    protected volatile int                   limit        = 0;
    protected int                            updateSpeed  = 2000;
    protected volatile int                   bandwidth    = 0;
    protected SpeedMeterInterface            speedMeter   = new AverageSpeedMeter(10);
    protected volatile long                  traffic      = 0;

    private final Object                     watchDogLOCK = new Object();
    private final String                     name;

    public SimpleThrottledConnectionHandler(final String name) {
        this.name = name;
    }

    @Override
    public void addThrottledConnection(final ThrottledConnection con) {
        if (this.connections.contains(con)) { return; }
        synchronized (this) {
            final java.util.List<ThrottledConnection> newConnections = new ArrayList<ThrottledConnection>(this.connections);
            newConnections.add(con);
            this.connections = newConnections;
        }
        con.setHandler(this);
        /*
         * we set very low limit here because we want the real speed to get
         * assigned on next speed-assign-loop
         */
        con.setLimit(10);
        this.startWatchDog();
    }

    @Override
    public List<ThrottledConnection> getConnections() {
        return this.connections;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }

    @Override
    public int getSpeed() {
        return this.bandwidth;
    }

    public SpeedMeterInterface getSpeedMeter() {
        return this.speedMeter;
    }

    @Override
    public long getTraffic() {
        return this.traffic;
    }

    @Override
    public void removeThrottledConnection(final ThrottledConnection con) {
        if (!this.connections.contains(con)) { return; }
        synchronized (this) {
            final java.util.List<ThrottledConnection> newConnections = new ArrayList<ThrottledConnection>(this.connections);
            newConnections.remove(con);
            this.connections = newConnections;
        }
        con.setHandler(null);
    }

    @Override
    public void setLimit(final int limit) {
        this.limit = Math.max(0, limit);

    }

    @Override
    public int size() {
        return this.connections.size();
    }

    private void startWatchDog() {
        synchronized (this.watchDogLOCK) {
            if (this.watchDog != null && this.watchDog.isAlive()) { return; }
            this.watchDog = new Thread() {
                @Override
                public void run() {
                    this.setName(SimpleThrottledConnectionHandler.this.name);
                    /* reset SpeedMeter */
                    SimpleThrottledConnectionHandler.this.speedMeter.resetSpeedMeter();
                    final HashMap<ThrottledConnection, SpeedAssignHelp> speedAssignHelpMap = new HashMap<ThrottledConnection, SpeedAssignHelp>();
                    while (true) {
                        final java.util.List<ThrottledConnection> lConnections = SimpleThrottledConnectionHandler.this.connections;
                        if (lConnections.size() == 0) {
                            break;
                        }
                        final long sleepTime = Math.max(1000, SimpleThrottledConnectionHandler.this.updateSpeed);
                        try {
                            Thread.sleep(sleepTime);
                        } catch (final InterruptedException e) {
                            Log.exception(e);
                        }
                        long lastTraffic = 0;
                        int newBandwidth = 0;
                        long lastRound = 0;
                        long lastRoundTraffic = 0;
                        for (final ThrottledConnection con : lConnections) {
                            SpeedAssignHelp helper = speedAssignHelpMap.get(con);
                            if (helper == null) {
                                helper = new SpeedAssignHelp();
                                speedAssignHelpMap.put(con, helper);
                                helper.lastTraffic = con.transfered();
                                helper.lastTimeStamp = System.currentTimeMillis();
                            } else {
                                final long sleepTimeCon = System.currentTimeMillis() - helper.lastTimeStamp;
                                lastTraffic = con.transfered();
                                helper.lastTimeStamp = System.currentTimeMillis();
                                /* update new traffic stats */
                                SimpleThrottledConnectionHandler.this.traffic += lastRound = lastTraffic - helper.lastTraffic;
                                lastRoundTraffic += helper.lastTraffic = lastTraffic;
                                /* update new bandwidth stats */
                                newBandwidth += helper.lastSpeed = (int) (lastRound * 1000 / sleepTimeCon);
                            }
                        }
                        SimpleThrottledConnectionHandler.this.bandwidth = newBandwidth;
                        SimpleThrottledConnectionHandler.this.speedMeter.putSpeedMeter(lastRoundTraffic, sleepTime);
                        int left = lConnections.size();
                        int limitLeft = SimpleThrottledConnectionHandler.this.limit;
                        for (final ThrottledConnection con : lConnections) {
                            final SpeedAssignHelp helper = speedAssignHelpMap.get(con);
                            helper.lastLimit = helper.newLimit;
                            helper.newLimit = limitLeft / left;
                            left--;
                            limitLeft -= helper.newLimit;
                        }
                        for (final ThrottledConnection con : lConnections) {
                            final SpeedAssignHelp helper = speedAssignHelpMap.get(con);
                            con.setLimit(helper.newLimit);
                        }
                    }
                    synchronized (SimpleThrottledConnectionHandler.this.watchDogLOCK) {
                        SimpleThrottledConnectionHandler.this.watchDog = null;
                        SimpleThrottledConnectionHandler.this.bandwidth = 0;
                        SimpleThrottledConnectionHandler.this.speedMeter.resetSpeedMeter();
                    }
                }
            };
            this.watchDog.setDaemon(false);
            this.watchDog.start();
        }
    }
}
