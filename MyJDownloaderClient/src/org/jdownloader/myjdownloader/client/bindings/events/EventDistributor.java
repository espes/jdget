package org.jdownloader.myjdownloader.client.bindings.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.jdownloader.myjdownloader.client.AbstractMyJDClientForBasicJVM;
import org.jdownloader.myjdownloader.client.bindings.events.json.MyJDEvent;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionResponse;
import org.jdownloader.myjdownloader.client.bindings.interfaces.EventsInterface;
import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;

public class EventDistributor implements Runnable, EventsDistributorListener {



    private EventsInterface                      link;

    private SubscriptionResponse                 subscription;
    private EventsDistributorEventSender         eventSender;

    private ArrayList<EventsDistributorListener> listeners;
    private long                                 polltimeout;
    private long                                 keepalive;

    public EventDistributor(final AbstractMyJDClientForBasicJVM api, final String deviceID) {

        link = api.link(EventsInterface.class, deviceID);

        listeners = new ArrayList<EventsDistributorListener>();

        eventSender = new EventsDistributorEventSender();
        eventSender.addListener(this);
    }

    public void removeListener(final EventsDistributorListener listener) {
        final ArrayList<EventsDistributorListener> newListeners = new ArrayList<EventsDistributorListener>();
        boolean changed = false;

        for (final EventsDistributorListener fi : listeners) {
            if (listener == fi) {
                // remove,
                changed = true;
            } else {
                newListeners.add(fi);

            }
        }
        listeners = newListeners;
        if (changed) {
            updateChannel();
        }

    }

    public void addListener(final EventsDistributorListener listener) {

        final ArrayList<EventsDistributorListener> newListeners = new ArrayList<EventsDistributorListener>();

        newListeners.addAll(listeners);
        newListeners.add(listener);
        listeners = newListeners;
        updateChannel();

    }

    // /**
    // *
    // * @param events
    // * list of regular expressions . define which events you want. for example .* for all
    // * @param blacklist
    // * list of regexes to define events you do NOT want. for example captcha\\.* do ignore all captcha events
    // */
    // protected void subscribe(final String[] events, final String[] blacklist) {
    // boolean changed = false;
    // if (events != null) {
    // for (final String s : events) {
    // changed |= subscribes.add(s);
    // }
    // }
    //
    // if (blacklist != null) {
    // for (final String s : blacklist) {
    // changed |= excludes.add(s);
    // }
    // }
    //
    // if (changed) {
    // if (running.get()) {
    // updateChannel();
    // }
    //
    // }
    // }

    private void updateChannel() {
        synchronized (this) {
            try {
                final HashSet<String> subscribes = new HashSet<String>();

                for (final EventsDistributorListener fi : listeners) {

                    if (fi.getEventPattern() != null) {

                        subscribes.add(fi.getEventPattern());

                    }

                }

                if (subscribes.size() == 0) {
                    close();
                    return;
                }
                if (subscription != null) {
                    SubscriptionResponse resp = link.setsubscription(subscription.getSubscriptionid(), subscribes.toArray(new String[] {}), null);
                    resp = updateConnectionSetup(resp);
                    if (resp != null && resp.getSubscriptionid() >= 0) {
                        subscription = resp;
                        printSub();
                        return;
                    }
                }
                close();
                System.out.println("Subscribe ");
                SubscriptionResponse resp = link.subscribe(subscribes.toArray(new String[] {}), null);
                if (resp != null && resp.getSubscriptionid() >= 0) {
                    resp = updateConnectionSetup(resp);
                    subscription = resp;
                    printSub();
                    return;
                }

            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }

    }

    public SubscriptionResponse updateConnectionSetup(SubscriptionResponse resp) {
        if ((polltimeout > 0 && polltimeout != resp.getMaxPolltimeout()) || (keepalive > 0 && keepalive != resp.getMaxKeepalive())) {
            final long pt = polltimeout > 0 ? polltimeout : resp.getMaxPolltimeout();
            final long ka = keepalive > 0 ? keepalive : resp.getMaxKeepalive();
            resp = link.changesubscriptiontimeouts(resp.getSubscriptionid(), pt, ka);
        }
        return resp;
    }

    private void close() {
        synchronized (this) {
            if (subscription != null) {
                System.out.println("Unsubscribe " + subscription.getSubscriptionid());
                link.unsubscribe(subscription.getSubscriptionid());

                subscription = null;
            }
        }
    }

    public void setConnectionConfig(final long polltimeout, final long keepalive) {

        this.polltimeout = polltimeout;
        this.keepalive = keepalive;
        synchronized (this) {

            if (subscription != null) {
                final SubscriptionResponse resp = updateConnectionSetup(subscription);
                if (resp != null && resp.getSubscriptionid() >= 0) {
                    subscription = resp;
                    printSub();

                }
            }

        }
    }

    private void printSub() {
        System.out.println("Channel Update" + subscription.getSubscriptionid());
        System.out.println("Subscriptions: " + Arrays.toString(subscription.getSubscriptions()));
        System.out.println("Excludes: " + Arrays.toString(subscription.getExclusions()));
        System.out.println("getMaxKeepalive: " + subscription.getMaxKeepalive());
        System.out.println("getMaxPolltimeout: " + subscription.getMaxPolltimeout());

    }

    private AtomicBoolean running  = new AtomicBoolean(false);
    private AtomicBoolean stopping = new AtomicBoolean(false);

    public void stop() {
        stopping.set(true);
        close();
    }

    public void run() {
        if (running.get()) { throw new RuntimeException("Already running!"); }
        running.set(true);
        stopping.set(false);
        try {

            while (true) {
                if (stopping.get()) {
                    stopping.set(false);
                    return;
                }
                try {
                    while (subscription == null) {
                        try {
                            updateChannel();
                            synchronized (this) {
                                if (subscription == null) {

                                    wait(10000);

                                }
                            }
                        } catch (final InterruptedException e) {
                            return;
                        }

                    }

                    while (true) {

                        final MyJDEvent[] result = link.listen(subscription.getSubscriptionid());

                        if (result != null) {
                            for (final MyJDEvent eos : result) {

                                eventSender.fireEvent(new NewEventDistributorEvent(this, eos));
                            }
                        }
                    }
                } catch (final ApiFileNotFoundException e) {
                    System.out.println("Channel Closed: " + subscription.getSubscriptionid());
                    subscription = null;

                } catch (final Exception e) {
                    e.printStackTrace();
                }

            }
        } finally {
            running.set(false);
        }

    }

    @Override
    public void onNewMyJDEvent(final String publisher, final String eventid, final Object eventData) {

        for (final EventsDistributorListener fi : listeners) {
            if (Pattern.compile(fi.getEventPattern()).matcher(publisher + "." + eventid).matches()) {
                if (!(fi.getFilterPattern() != null && fi.getFilterPattern().trim().length() > 0 && Pattern.compile(fi.getFilterPattern()).matcher(publisher + "." + eventid).matches())) {
                    fi.onNewMyJDEvent(publisher, eventid, eventData);
                }
            }
        }
    }

    @Override
    public String getEventPattern() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getFilterPattern() {
        throw new RuntimeException("Not implemented");
    }

}
