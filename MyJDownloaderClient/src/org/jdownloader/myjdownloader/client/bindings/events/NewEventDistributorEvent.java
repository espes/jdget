package org.jdownloader.myjdownloader.client.bindings.events;

import org.jdownloader.myjdownloader.client.bindings.events.json.MyJDEvent;

public class NewEventDistributorEvent extends AbstractEventsDistributorEvent {

    private MyJDEvent event;

    public NewEventDistributorEvent(final Object caller, final MyJDEvent eos) {
        super(caller, Type.NEW, eos);
        event = eos;
    }

    @Override
    protected void sendTo(final EventsDistributorListener listener) {
        listener.onNewMyJDEvent(event.getPublisher(), event.getEventid(), event.getEventData());
    }
}
