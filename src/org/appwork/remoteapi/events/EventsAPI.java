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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.remoteapi.RemoteAPI;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.events.json.EventObject;
import org.appwork.remoteapi.events.json.PublisherResponse;
import org.appwork.remoteapi.events.json.SubscriptionResponse;
import org.appwork.storage.JSonStorage;

/**
 * @author daniel
 * 
 */
public class EventsAPI implements EventsAPIInterface {

    protected final ConcurrentHashMap<Long, Subscriber> subscribers            = new ConcurrentHashMap<Long, Subscriber>(8, 0.9f, 1);
    protected EventPublisher[]                          publishers             = new EventPublisher[0];
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

    public EventPublisher[] list() {
        return this.publishers.clone();
    }

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, final long subscriptionid) {
        this.listen(request, response, subscriptionid, 0);
    }

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, final long subscriptionid, long lasteventnumber) {
        final Subscriber subscriber = this.subscribers.get(subscriptionid);
        if (subscriber == null) {
            response.setResponseCode(ResponseCode.ERROR_NOT_FOUND);
            return;
        }
        final ArrayList<EventObject> events = new ArrayList<EventObject>();
        try {
            EventObject event;
            while ((event = subscriber.poll(events.size() == 0 ? subscriber.getPollTimeout() : 0)) != null) {
                if (lasteventnumber > 0) {
                    if (event.getEventnumber() != lasteventnumber) {
                        this.subscribers.remove(subscriptionid);
                        response.setResponseCode(ResponseCode.ERROR_NOT_FOUND);
                        return;
                    } else {
                        lasteventnumber = 0;
                    }
                }
                events.add(event);
            }
        } catch (final InterruptedException e) {
        }
        try {
            final byte[] bytes = JSonStorage.toString(events).getBytes("UTF-8");
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            RemoteAPI.sendBytes(response, RemoteAPI.gzip(request), false, bytes);
        } catch (final Throwable e) {
            subscriber.pushBack(events);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<PublisherResponse> listpublisher() {
        final ArrayList<PublisherResponse> ret = new ArrayList<PublisherResponse>();
        final EventPublisher[] lpublishers = this.publishers;
        for (final EventPublisher publisher : lpublishers) {
            ret.add(new PublisherResponse(publisher));
        }
        return ret;
    }

    public void publishEvent(final EventObject event, final long[] subscriptionids) {
        ArrayList<Subscriber> publishTo = new ArrayList<Subscriber>();
        if (subscriptionids != null && subscriptionids.length > 0) {
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
                subscriber.push(event);
            }
        }
    }

    public synchronized boolean register(final EventPublisher publisher) {
        if (publisher == null) { throw new NullPointerException(); }
        if (publisher.getPublisherName() == null) { throw new IllegalArgumentException("no Publishername given"); }
        final ArrayList<EventPublisher> existingPublishers = new ArrayList<EventPublisher>(Arrays.asList(this.publishers));
        if (existingPublishers.contains(publisher)) { return false; }
        for (final EventPublisher existingPublisher : existingPublishers) {
            if (publisher.getPublisherName().equalsIgnoreCase(existingPublisher.getPublisherName())) { throw new IllegalArgumentException("publisher with same name already registered"); }
        }
        existingPublishers.add(publisher);
        this.publishers = existingPublishers.toArray(new EventPublisher[] {});
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
        final ArrayList<EventPublisher> existingPublishers = new ArrayList<EventPublisher>(Arrays.asList(this.publishers));
        final boolean removed = existingPublishers.remove(publisher);
        this.publishers = existingPublishers.toArray(new EventPublisher[] {});
        return removed;
    }

    @Override
    public SubscriptionResponse unsubscribe(final long subscriptionid) {
        final Subscriber subscriber = this.subscribers.remove(subscriptionid);
        if (subscriber != null) {
            subscriber.notifyPoll();
            return new SubscriptionResponse(subscriber);
        }
        return new SubscriptionResponse();
    }
}
