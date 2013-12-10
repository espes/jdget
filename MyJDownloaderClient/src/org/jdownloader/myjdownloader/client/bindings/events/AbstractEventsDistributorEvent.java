package org.jdownloader.myjdownloader.client.bindings.events;

import org.jdownloader.myjdownloader.client.eventsender.SimpleEvent;

public abstract class AbstractEventsDistributorEvent extends SimpleEvent<Object, Object, AbstractEventsDistributorEvent.Type> {

    public static enum Type {
        NEW
    }

    public AbstractEventsDistributorEvent(final Object caller, final Type type, final Object... parameters) {
        super(caller, type, parameters);
    }

    protected abstract void sendTo(final EventsDistributorListener listener) ;
  
}