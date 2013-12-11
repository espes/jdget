package org.jdownloader.myjdownloader.client.bindings.events;

import org.jdownloader.myjdownloader.client.AbstractMyJDClient;
import org.jdownloader.myjdownloader.client.bindings.events.json.MyJDEvent;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionResponse;
import org.jdownloader.myjdownloader.client.bindings.interfaces.EventsAPIInterface;

public class EventDistributor implements Runnable {

    private AbstractMyJDClient           api;
    private EventsAPIInterface           link;
    private String[]                     events;
    private String[]                     blacklist;
    private SubscriptionResponse         subscription;
    private EventsDistributorEventSender eventSender;

    public EventDistributor(final AbstractMyJDClient api, final String deviceID) {
        this.api = api;
        link = api.link(EventsAPIInterface.class, deviceID);
        eventSender = new EventsDistributorEventSender();
    }

    public EventsDistributorEventSender getEventSender() {
        return eventSender;
    }

    /**
     * 
     * @param events
     *            list of regular expressions . define which events you want. for example .* for all
     * @param blacklist
     *            list of regexes to define events you do NOT want. for example captcha\\.* do ignore all captcha events
     */
    public void subscribe(final String[] events, final String[] blacklist) {
        this.events = events;
        this.blacklist = blacklist;

    }

    public void run() {
        subscription = link.subscribe(events, blacklist);

        while (true) {
            final MyJDEvent[] result = link.listen(subscription.getSubscriptionid());
            if (result != null) {
                for (final MyJDEvent eos : result) {

                    eventSender.fireEvent(new NewEventDistributorEvent(this,eos));
                }
            }
        }

    }

}
