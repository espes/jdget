/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event;

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
    private Object caller;
    /**
     * ID of this Event
     */
    private int eventID;

    /**
     * Parameters of this event.
     */
    private Object parameter = null;

    /**
     * Creates a new Event
     * 
     * @param caller
     *            The Object that fires this event
     * @param eventID
     *            The Event's id
     */
    public DefaultEvent(Object caller, int eventID) {
        this.caller = caller;
        this.eventID = eventID;
    }

    /**
     * 
     * @param caller
     *            The Object that fires this event
     * @param eventID
     *            The Event's id
     * @param parameter
     *            a parameter object
     */
    public DefaultEvent(Object caller, int eventID, Object parameter) {
        this(caller, eventID);
        this.parameter = parameter;
    }

    /**
     * @return the {@link DefaultEvent#caller}
     * @see DefaultEvent#caller
     */
    public Object getCaller() {
        return caller;
    }

    /**
     * @return the {@link DefaultEvent#eventID}
     * @see DefaultEvent#eventID
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * @return the {@link DefaultEvent#parameter}
     * @see DefaultEvent#parameter
     */
    public Object getParameter() {
        return parameter;
    }

}
