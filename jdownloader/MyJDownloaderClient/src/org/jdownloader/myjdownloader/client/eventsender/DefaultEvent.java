package org.jdownloader.myjdownloader.client.eventsender;
/**
 * Abstract Eventclass. All Events should be derived from this class to asuire
 * compatibility to the EventSystem.
 * 
 * @author $Author: unknown$
 * 
 */
public abstract class DefaultEvent {
    /**
     * The caller that fired this event
     */
    private final Object caller;

    /**
     * Creates a new Event
     * 
     * @param caller
     *            The Object that fires this event
     */
    public DefaultEvent(final Object caller) {
        this.caller = caller;
    }

    /**
     * @return the {@link DefaultEvent#caller}
     * @see DefaultEvent#caller
     */
    public Object getCaller() {
        return caller;
    }
}
