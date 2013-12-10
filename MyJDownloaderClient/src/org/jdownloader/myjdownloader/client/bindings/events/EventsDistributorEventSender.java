package org.jdownloader.myjdownloader.client.bindings.events;

import org.jdownloader.myjdownloader.client.eventsender.Eventsender;

public class EventsDistributorEventSender extends Eventsender<EventsDistributorListener, AbstractEventsDistributorEvent> {

    @Override
    protected void fireEvent(final EventsDistributorListener listener, final AbstractEventsDistributorEvent event) {
        event.sendTo(listener);
  
    }
}