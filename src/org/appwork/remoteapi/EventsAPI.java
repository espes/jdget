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

import java.util.HashMap;

import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public abstract class EventsAPI<T extends HttpSession> implements EventsAPIInterface {

    private final HashMap<T, EventsAPIQueue<T>> eventQueues = new HashMap<T, EventsAPIQueue<T>>();

    protected HashMap<String, Object> doDisconnect() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("type", "system");
        return ret;
    }

    protected HashMap<String, Object> doHeartbeat() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("type", "system");
        ret.put("message", "heartbeat");
        return ret;
    }

    protected HashMap<String, Object> doUnauthorized() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("type", "system");
        ret.put("message", "unauthorized");
        return ret;
    }

    public abstract boolean isSessionAllowed(T session);

    @SuppressWarnings("unchecked")
    public HashMap<String, Object> listen(final RemoteAPIRequest request) {
        if (!(request instanceof SessionRemoteAPIRequest)) { return this.doUnauthorized(); }
        final SessionRemoteAPIRequest<T> sr = (SessionRemoteAPIRequest<T>) request;
        if (!this.isSessionAllowed(sr.getSession())) { return this.doUnauthorized(); }
        EventsAPIQueue<T> queue = null;
        synchronized (this) {
            queue = this.eventQueues.get(sr.getSession());
            if (queue == null) {
                queue = new EventsAPIQueue<T>(sr.getSession());
                this.eventQueues.put(sr.getSession(), queue);
            }
        }
        HashMap<String, Object> ret = new HashMap<String, Object>();
        synchronized (queue) {
            try {
                queue.wait(30 * 1000l);
            } catch (final InterruptedException e) {
            }
        }
        if (ret.size() == 0) {
            /* no events to send, send heartbeat */
            ret = this.doHeartbeat();
        }
        return ret;
    }

    public void publishEvent(final Object event, final T... receivers) {

    }

}
