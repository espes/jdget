/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.util.LinkedList;

/**
 * @author daniel
 * 
 */
public class EventsAPIQueue {

    private long                             lastPushTimestamp = 0;
    private long                             lastPushID        = 0;
    private final long                       messageID         = 0;
    private final LinkedList<EventsAPIEvent> events            = new LinkedList<EventsAPIEvent>();
    private long                             lastPullTimeStamp = 0;
    private long                             lastPullID        = 0;

    public EventsAPIQueue() {
    }

    public synchronized EventsAPIEvent pullEvent() {
        if (this.events.isEmpty()) { return null; }
        final EventsAPIEvent ret = this.events.removeFirst();
        this.lastPullTimeStamp = System.currentTimeMillis();
        this.lastPullID = ret.getMessageID();
        return ret;
    }

    protected synchronized void pushBackEvent(EventsAPIEvent event) {
        if (event == null) { return; }
        /*
         * we clone event to avoid multiple usage of same event in different
         * queues
         */
        event = event.clone();
        this.events.addFirst(event);
    }

    public synchronized void pushEvent(EventsAPIEvent event) {
        if (event == null) { return; }
        /*
         * we clone event to avoid multiple usage of same event in different
         * queues
         */
        event = event.clone();
        this.events.add(event);
        event.setMessageID(++this.lastPushID);
        this.lastPushTimestamp = System.currentTimeMillis();
    }

}
