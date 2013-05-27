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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
    protected long                          pollTimeout         = 30 * 1000l;
    protected long                          maxKeepalive        = 120 * 1000l;

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
        final String eventID = event.getPublisher().getPublisherName() + "." + event.getEventid();
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

    protected void keepAlive() {
        this.lastPolledTimestamp = System.currentTimeMillis();
    }

    protected void notifyListener() {
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

    protected void push(final EventObject event) {
        if (event == null) { return; }
        synchronized (this.events) {
            if (event.getCollapseKey() != null) {
                /*
                 * event has a collapseKey, so let's search for existing event
                 * to replace/remove
                 */
                final Iterator<EventObject> it = this.events.descendingIterator();
                while (it.hasNext()) {
                    final EventObject next = it.next();
                    if (next.getCollapseKey() != null && next.getCollapseKey().equals(event.getCollapseKey())) {
                        it.remove();
                        break;
                    }
                }
            }
            this.events.offerLast(event);
        }
    }

    protected void pushBack(final List<EventObject> pushBackEvents) {
        if (pushBackEvents.size() == 0) { return; }
        synchronized (this.events) {
            if (this.events.size() == 0) {
                /*
                 * fast, current eventqueue is empty, so we can pushBack all at
                 * once
                 */
                this.events.addAll(pushBackEvents);
                return;
            }
            final ArrayList<EventObject> addFirst = new ArrayList<EventObject>(pushBackEvents.size());
            addFirstLoop: for (final EventObject pushBackEvent : pushBackEvents) {
                if (pushBackEvent.getCollapseKey() != null) {
                    for (final EventObject currentEvent : this.events) {
                        if (currentEvent.getCollapseKey() != null && currentEvent.getCollapseKey().equals(pushBackEvent.getCollapseKey())) {
                            continue addFirstLoop;
                        }
                    }
                }
                addFirst.add(pushBackEvent);
            }
            if (addFirst.size() == 0) {
                /* all pushBackEvents were collapsed */
                return;
            }
            /*
             * clear current eventqueue and add all pushBack ones first, then
             * the backup of current ones
             */
            final ArrayList<EventObject> backup = new ArrayList<EventObject>(this.events);
            this.events.clear();
            this.events.addAll(addFirst);
            this.events.addAll(backup);
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
    protected void setMaxKeepalive(long maxKeepalive) {
        maxKeepalive = Math.min(maxKeepalive, 3600 * 1000l);
        maxKeepalive = Math.max(maxKeepalive, 30 * 1000l);
        this.maxKeepalive = maxKeepalive;
    }

    /**
     * @param pollTimeout
     *            the pollTimeout to set
     */
    protected void setPollTimeout(long pollTimeout) {
        /*
         * http://gabenell.blogspot.de/2010/11/connection-keep-alive-timeouts-for
         * .html
         */
        pollTimeout = Math.min(pollTimeout, 360 * 1000l);
        pollTimeout = Math.max(pollTimeout, 15 * 1000l);
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
