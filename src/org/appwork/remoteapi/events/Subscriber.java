/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.events
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.remoteapi.events.json.EventObject;
import org.appwork.utils.Regex;

/**
 * @author daniel
 * 
 */
public class Subscriber {

    protected static final AtomicLong       SUBSCRIBER          = new AtomicLong(0);
    protected String[]                      subscriptions;
    protected String[]                      exclusions;
    protected final ArrayDeque<EventObject> events              = new ArrayDeque<EventObject>();
    protected final long                    subscriptionID;
    protected long                          lastPolledTimestamp = System.currentTimeMillis();
    protected long                          eventnumber         = 0;
    protected long                          pollTimeout         = 30 * 1000l;
    protected long                          maxKeepalive        = 60 * 1000l;

    protected Subscriber(final String[] subscriptions, final String[] exclusions) {
        this.setSubscriptions(subscriptions);
        this.setExclusions(exclusions);
        this.subscriptionID = Subscriber.SUBSCRIBER.incrementAndGet();
    }

    public String[] getExclusions() {
        return this.exclusions.clone();
    }

    public long getLastPolledTimestamp() {
        return this.lastPolledTimestamp;
    }

    /**
     * @return the maxKeepalive
     */
    public long getMaxKeepalive() {
        return this.maxKeepalive;
    }

    protected Object getModifyLock() {
        return this;
    }

    /**
     * @return the pollTimeout
     */
    public long getPollTimeout() {
        return this.pollTimeout;
    }

    public long getSubscriptionID() {
        return this.subscriptionID;
    }

    public String[] getSubscriptions() {
        return this.subscriptions.clone();
    }

    protected boolean isSubscribed(final EventObject event) {
        if (this.subscriptions.length == 0) {
            /* no subscriptions = no interest in any event */
            return false;
        }
        final String eventID = event.getPublisher() + "." + event.getEventid();
        for (final String subscription : this.subscriptions) {
            try {
                if (new Regex(eventID, subscription).matches()) {
                    /* we have a subscription match */
                    for (final String exclusion : this.exclusions) {
                        try {
                            if (new Regex(eventID, exclusion).matches()) {
                                /*
                                 * there exists an exclusion, no interest in
                                 * this event
                                 */
                                return false;
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected void notifyPoll() {
        synchronized (this.events) {
            this.events.notifyAll();
        }
    }

    protected EventObject poll(final long waitfor) throws InterruptedException {
        synchronized (this.events) {
            this.lastPolledTimestamp = System.currentTimeMillis();
            EventObject ret = this.events.poll();
            if (ret == null && waitfor > 0) {
                this.events.wait(waitfor);
                ret = this.events.poll();
            }
            return ret;
        }
    }

    protected void push(EventObject event) {
        if (event == null) { return; }
        event = event.clone();
        synchronized (this.events) {
            this.events.offerLast(event);
            event.setEventnumber(++this.eventnumber);
            this.events.notifyAll();
        }
    }

    protected void pushBack(final List<EventObject> events) {
        synchronized (this.events) {
            Collections.reverse(events);
            for (final EventObject event : events) {
                this.events.offerFirst(event);
            }
        }
    }

    protected void setExclusions(final String[] exclusions) {
        if (exclusions == null) {
            this.exclusions = new String[0];
        } else {
            this.exclusions = this.uniquify(exclusions);
        }
    }

    /**
     * @param maxKeepalive
     *            the maxKeepalive to set
     */
    public void setMaxKeepalive(long maxKeepalive) {
        maxKeepalive = Math.min(maxKeepalive, 300 * 1000l);
        maxKeepalive = Math.max(maxKeepalive, 60 * 1000l);
        this.maxKeepalive = maxKeepalive;
    }

    /**
     * @param pollTimeout
     *            the pollTimeout to set
     */
    public void setPollTimeout(long pollTimeout) {
        /*
         * http://gabenell.blogspot.de/2010/11/connection-keep-alive-timeouts-for
         * .html
         */
        pollTimeout = Math.min(pollTimeout, 150 * 1000l);
        pollTimeout = Math.max(pollTimeout, 30 * 1000l);
        this.pollTimeout = pollTimeout;
    }

    protected void setSubscriptions(final String[] subscriptions) {
        if (subscriptions == null) {
            this.subscriptions = new String[0];
        } else {
            this.subscriptions = this.uniquify(subscriptions);
        }
    }

    public int size() {
        synchronized (this.events) {
            return this.events.size();
        }
    }

    private String[] uniquify(final String[] input) {
        if (input == null) { return null; }
        return new HashSet<String>(Arrays.asList(input)).toArray(new String[] {});
    }

}
