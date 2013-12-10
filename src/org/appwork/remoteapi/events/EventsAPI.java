/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.events.json.EventObjectStorable;
import org.appwork.remoteapi.events.json.PublisherResponse;
import org.appwork.remoteapi.events.json.SubscriptionResponse;
import org.appwork.remoteapi.events.json.SubscriptionStatusResponse;

/**
 * @author daniel
 * 
 */
public class EventsAPI implements EventsAPIInterface, EventsSender {

    protected final ConcurrentHashMap<Long, Subscriber> subscribers            = new ConcurrentHashMap<Long, Subscriber>(8, 0.9f, 1);
    protected CopyOnWriteArrayList<EventPublisher>      publishers             = new CopyOnWriteArrayList<EventPublisher>();
    protected final Object                              subscribersCleanupLock = new Object();
    protected Thread                                    cleanupThread          = null;

    @Override
    public SubscriptionResponse addsubscription(final long subscriptionid, final String[] subscriptions, final String[] exclusions) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionResponse();
        } else {
            synchronized (subscriber.getModifyLock()) {
                if (exclusions != null) {
                    final ArrayList<String> newExclusions = new ArrayList<String>(Arrays.asList(subscriber.getExclusions()));
                    newExclusions.addAll(Arrays.asList(exclusions));
                    subscriber.setExclusions(newExclusions.toArray(new String[] {}));
                }
                if (subscriptions != null) {
                    final ArrayList<String> newSubscriptions = new ArrayList<String>(Arrays.asList(subscriber.getSubscriptions()));
                    newSubscriptions.addAll(Arrays.asList(subscriptions));
                    subscriber.setSubscriptions(newSubscriptions.toArray(new String[] {}));
                }
            }
            final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    @Override
    public SubscriptionResponse changesubscriptiontimeouts(final long subscriptionid, final long polltimeout, final long maxkeepalive) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionResponse();
        } else {
            subscriber.setMaxKeepalive(maxkeepalive);
            subscriber.setPollTimeout(polltimeout);
            subscriber.notifyListener();
            final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    @Override
    public SubscriptionResponse getsubscription(final long subscriptionid) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionResponse();
        } else {
            final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.remoteapi.events.EventsAPIInterface#getsubscriptionstatus
     * (long)
     */
    @Override
    public SubscriptionStatusResponse getsubscriptionstatus(final long subscriptionid) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionStatusResponse();
        } else {
            subscriber.keepAlive();
            final SubscriptionStatusResponse ret = new SubscriptionStatusResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    public List<EventPublisher> list() {
        return Collections.unmodifiableList(this.publishers);
    }

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, final long subscriptionid) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            response.setResponseCode(ResponseCode.ERROR_NOT_FOUND);
            return;
        }
        final ArrayList<EventObject> events = new ArrayList<EventObject>();
        final ArrayList<EventObjectStorable> eventStorables = new ArrayList<EventObjectStorable>();
        try {
            EventObject event;
            while ((event = subscriber.poll(events.size() == 0 ? subscriber.getPollTimeout() : 0)) != null && this.subscribers.get(subscriptionid) == subscriber) {
                events.add(event);
                eventStorables.add(new EventObjectStorable(event));
            }
        } catch (final InterruptedException e) {
        }
        try {
            response.getRemoteAPI().writeStringResponse(eventStorables, null, false, request, response);
        } catch (final Throwable e) {
            subscriber.pushBack(events);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PublisherResponse> listpublisher() {
        final ArrayList<PublisherResponse> ret = new ArrayList<PublisherResponse>();
        for (final EventPublisher publisher : this.publishers) {
            ret.add(new PublisherResponse(publisher));
        }
        return ret;
    }

    public List<Long> publishEvent(final EventObject event, final List<Long> subscriptionids) {
        ArrayList<Subscriber> publishTo = new ArrayList<Subscriber>();
        final ArrayList<Long> ret = new ArrayList<Long>();
        if (subscriptionids != null && subscriptionids.size() > 0) {
            /* publish to given subscriptionids */
            for (final long subscriptionid : subscriptionids) {
                final Subscriber subscriber = this.subscribers.get(subscriptionid);
                if (subscriber != null) {
                    publishTo.add(subscriber);
                }
            }
        } else {
            /* publish to all subscribers */
            publishTo = new ArrayList<Subscriber>(this.subscribers.values());
        }
        for (final Subscriber subscriber : publishTo) {
            if (subscriber.isSubscribed(event)) {
                ret.add(subscriber.getSubscriptionID());
                subscriber.push(event);
                subscriber.notifyListener();
            }
        }
        return ret;
    }

    public synchronized boolean register(final EventPublisher publisher) {
        if (publisher == null) { throw new NullPointerException(); }
        if (publisher.getPublisherName() == null) { throw new IllegalArgumentException("no Publishername given"); }
        for (final EventPublisher existingPublisher : this.publishers) {
            if (existingPublisher == publisher) { return false; }
            if (publisher.getPublisherName().equalsIgnoreCase(existingPublisher.getPublisherName())) { throw new IllegalArgumentException("publisher with same name already registered"); }
        }
        this.publishers.add(publisher);
        publisher.register(this);
        return true;
    }

    @Override
    public SubscriptionResponse removesubscription(final long subscriptionid, final String[] subscriptions, final String[] exclusions) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionResponse();
        } else {
            synchronized (subscriber.getModifyLock()) {
                if (exclusions != null) {
                    final ArrayList<String> newExclusions = new ArrayList<String>(Arrays.asList(subscriber.getExclusions()));
                    newExclusions.removeAll(Arrays.asList(exclusions));
                    subscriber.setExclusions(newExclusions.toArray(new String[] {}));
                }
                if (subscriptions != null) {
                    final ArrayList<String> newSubscriptions = new ArrayList<String>(Arrays.asList(subscriber.getSubscriptions()));
                    newSubscriptions.removeAll(Arrays.asList(subscriptions));
                    subscriber.setSubscriptions(newSubscriptions.toArray(new String[] {}));
                }

            }
            final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    @Override
    public SubscriptionResponse setsubscription(final long subscriptionid, final String[] subscriptions, final String[] exclusions) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            return new SubscriptionResponse();
        } else {
            synchronized (subscriber.getModifyLock()) {
                final ArrayList<String> newExclusions = new ArrayList<String>();
                if (exclusions != null) {
                    newExclusions.addAll(Arrays.asList(exclusions));
                }
                subscriber.setExclusions(newExclusions.toArray(new String[] {}));

                final ArrayList<String> newSubscriptions = new ArrayList<String>();
                if (subscriptions != null) {
                    newSubscriptions.addAll(Arrays.asList(subscriptions));
                }
                subscriber.setSubscriptions(newSubscriptions.toArray(new String[] {}));
            }
            final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
            ret.setSubscribed(true);
            return ret;
        }
    }

    @Override
    public SubscriptionResponse subscribe(final String[] subscriptions, final String[] exclusions) {
        final Subscriber subscriber = new Subscriber(subscriptions, exclusions);
        this.subscribers.put(subscriber.getSubscriptionID(), subscriber);
        this.subscribersCleanupThread();
        final SubscriptionResponse ret = new SubscriptionResponse(subscriber);
        ret.setSubscribed(true);
        return ret;
    }

    /*
     * starts a cleanupThread (if needed) to remove subscribers that are no
     * longer alive
     * 
     * current implementation has a minimum delay of 1 minute
     */
    protected void subscribersCleanupThread() {
        synchronized (this.subscribersCleanupLock) {
            if (this.cleanupThread == null || this.cleanupThread.isAlive() == false) {
                this.cleanupThread = null;
            } else {
                return;
            }
            this.cleanupThread = new Thread("EventsAPI:subscribersCleanupThread") {
                @Override
                public void run() {
                    try {
                        while (Thread.currentThread() == EventsAPI.this.cleanupThread) {
                            try {
                                Thread.sleep(60 * 1000);
                                final Iterator<Entry<Long, Subscriber>> it = EventsAPI.this.subscribers.entrySet().iterator();
                                while (it.hasNext()) {
                                    final Entry<Long, Subscriber> next = it.next();
                                    final Subscriber subscriber = next.getValue();
                                    if (subscriber.getLastPolledTimestamp() + subscriber.getMaxKeepalive() < System.currentTimeMillis()) {
                                        it.remove();
                                        final long subscriptionid = subscriber.getSubscriptionID();
                                        try {
                                            for (final EventPublisher publisher : EventsAPI.this.publishers) {
                                                publisher.terminatedSubscription(EventsAPI.this, subscriptionid);
                                            }
                                        } catch (final Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                synchronized (EventsAPI.this.subscribersCleanupLock) {
                                    if (EventsAPI.this.subscribers.size() == 0) {
                                        EventsAPI.this.cleanupThread = null;
                                        break;
                                    }
                                }
                            } catch (final Throwable e) {
                            }
                        }
                    } finally {
                        synchronized (EventsAPI.this.subscribersCleanupLock) {
                            if (Thread.currentThread() == EventsAPI.this.cleanupThread) {
                                EventsAPI.this.cleanupThread = null;
                            }
                        }
                    }
                };
            };
            this.cleanupThread.setDaemon(true);
            this.cleanupThread.start();
        }
    }

    public synchronized boolean unregister(final EventPublisher publisher) {
        if (publisher == null) { throw new NullPointerException(); }
        final boolean removed = this.publishers.remove(publisher);
        publisher.unregister(this);
        return removed;
    }

    @Override
    public SubscriptionResponse unsubscribe(final long subscriptionid) {
        final Subscriber subscriber = this.subscribers.remove(subscriptionid);
        if (subscriber != null) {
            subscriber.notifyListener();
            try {
                for (final EventPublisher publisher : this.publishers) {
                    publisher.terminatedSubscription(this, subscriptionid);
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            }
            return new SubscriptionResponse(subscriber);
        }
        return new SubscriptionResponse();
    }
}
