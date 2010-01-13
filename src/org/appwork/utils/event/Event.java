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
public abstract class Event {
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
    public Event(Object caller, int eventID) {
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
    public Event(Object caller, int eventID, Object parameter) {
        this(caller, eventID);
        this.parameter = parameter;
    }

    /**
     * @return the {@link Event#caller}
     * @see Event#caller
     */
    public Object getCaller() {
        return caller;
    }

    /**
     * @return the {@link Event#eventID}
     * @see Event#eventID
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * @return the {@link Event#parameter}
     * @see Event#parameter
     */
    public Object getParameter() {
        return parameter;
    }

}
