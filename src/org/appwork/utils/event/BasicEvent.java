package org.appwork.utils.event;

import java.util.EventObject;

@Deprecated
public class BasicEvent<E> extends DefaultIntEvent<E> {

    private final EventObject sourceEvent;

    @SuppressWarnings({ "unchecked" })
    public BasicEvent(final Object source, final int i, final E parameter, final EventObject e) {
        super(source, i, parameter);
        this.sourceEvent = e;

    }

    /**
     * @return the {@link BasicEvent#sourceEvent}
     * @see BasicEvent#sourceEvent
     */
    public EventObject getSourceEvent() {
        return this.sourceEvent;
    }

}
