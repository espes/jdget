package org.appwork.utils.event;

import java.util.EventObject;

import org.appwork.utils.event.DefaultEvent;

public class BasicEvent<E> extends DefaultEvent {

    private EventObject sourceEvent;

    public BasicEvent(Object source, int i, E parameter, EventObject e) {
        super(source, i, parameter);
        sourceEvent = e;

    }

    @SuppressWarnings("unchecked")
    public E getParameter() {
        return (E) super.getParameter();
    }

    /**
     * @return the {@link BasicEvent#sourceEvent}
     * @see BasicEvent#sourceEvent
     */
    public EventObject getSourceEvent() {
        return sourceEvent;
    }

}
