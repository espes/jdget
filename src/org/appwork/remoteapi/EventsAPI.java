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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public abstract class EventsAPI implements EventsAPIInterface {

    private final HashMap<String, MinTimeWeakReference<EventsAPIQueue>> eventQueues  = new HashMap<String, MinTimeWeakReference<EventsAPIQueue>>();
    private final HashMap<String, WeakReference<HttpSession>>           sessionMap   = new HashMap<String, WeakReference<HttpSession>>();

    protected long                                                      queueTimeout = 5 * 60 * 1000;

    public abstract boolean isSessionAllowed(HttpSession session);

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response) {
        this.listen(request, response, -1l);
    }

    @Override
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, final Long lastEventID) {
        if (!(request instanceof SessionRemoteAPIRequest)) { throw new RemoteAPIUnauthorizedException(); }
        final SessionRemoteAPIRequest<? extends HttpSession> sr = (SessionRemoteAPIRequest<?>) request;
        String sessionID = null;
        if (sr.getSession() != null) {
            sessionID = sr.getSession().getSessionID();
        }
        if (!this.isSessionAllowed(sr.getSession()) || sessionID == null) {
            /* session is not allowed */
            synchronized (this) {
                if (sessionID != null) {
                    this.eventQueues.remove(sessionID);
                    this.sessionMap.remove(sessionID);
                }
            }
            throw new RemoteAPIUnauthorizedException();
        }
        if (sr.getSession() != null && !sr.getSession().isAlive()) {
            /* session no longer alive, remove it */
            synchronized (this) {
                if (sessionID != null) {
                    this.eventQueues.remove(sessionID);
                    this.sessionMap.remove(sessionID);
                }
            }
            throw new RemoteAPIUnauthorizedException();
        }
        /* response object */
        HashMap<String, Object> ret = new HashMap<String, Object>();

        EventsAPIQueue queue = null;
        synchronized (this) {
            final MinTimeWeakReference<EventsAPIQueue> mqueue = this.eventQueues.get(sessionID);
            if (mqueue == null || (queue = mqueue.get()) == null) {
                queue = new EventsAPIQueue();
                this.eventQueues.put(sessionID, new MinTimeWeakReference<EventsAPIQueue>(queue, this.queueTimeout, "EventQueue for" + sessionID));
            }
            this.sessionMap.put(sessionID, new WeakReference<HttpSession>(sr.getSession()));
        }
        EventsAPIEvent event = queue.pullEvent();
        synchronized (queue) {
            if (event == null) {
                try {
                    queue.wait(30 * 1000l);
                } catch (final InterruptedException e) {
                }
            }
        }
        if (event == null) {
            event = queue.pullEvent();
        }
        final java.util.List<HashMap<String, Object>> eventArray = new ArrayList<HashMap<String, Object>>();
        boolean checkID = lastEventID >= 0;
        while (event != null) {
            /* build json for this event */
            final HashMap<String, Object> eventJson = new HashMap<String, Object>();
            if (event.getProcessID() != null) {
                eventJson.put("pid", event.getProcessID());
            }
            eventJson.put("namespace", event.getNamespace());
            eventJson.put("messageid", event.getMessageID());
            if (checkID) {
                /* check if we are out of sync */
                if (event.getMessageID() != lastEventID + 1) {
                    /* we push back the event */
                    queue.pushBackEvent(event);
                    throw new RemoteAPIOutOfSyncException();
                }
                checkID = false;
            }
            eventJson.put("data", event.getData());
            /* add event to event list */
            eventArray.add(eventJson);
            ret.put("data", eventArray);
            ret.put("id", event.getMessageID());
            event = queue.pullEvent();
        }

        if (ret.size() == 0) {
            /* no events to send, send heartbeat */
            ret = new EventsAPIHeartbeat().getRemoteAPIExceptionResponse();
        }
        String text = JSonStorage.toString(ret);

        if (request.getJqueryCallback() != null) {
            /* wrap response into a valid jquery callback response format */
            final StringBuilder sb = new StringBuilder();
            sb.append(request.getJqueryCallback());
            sb.append("(");
            sb.append(text);
            sb.append(");");
            text = sb.toString();
        }

        try {
            final byte[] bytes = text.getBytes("UTF-8");
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            RemoteAPI.sendBytes(response, RemoteAPI.gzip(request), false, bytes);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void publishEvent(final EventsAPIEvent event, final List<HttpSession> receivers) {
        if (receivers != null) {
            for (final HttpSession receiver : receivers) {
                EventsAPIQueue queue = null;
                synchronized (this) {
                    if (!receiver.isAlive()) {
                        /* session no longer alive, remove it */
                        this.eventQueues.remove(receiver.getSessionID());
                        this.sessionMap.remove(receiver.getSessionID());
                        continue;
                    }
                    final MinTimeWeakReference<EventsAPIQueue> mqueue = this.eventQueues.get(receiver.getSessionID());
                    if (mqueue == null || (queue = mqueue.superget()) == null) {
                        /* we dont want to refresh mintimeweakreference */
                        this.eventQueues.remove(receiver.getSessionID());
                        this.sessionMap.remove(receiver.getSessionID());
                        continue;
                    }
                }
                queue.pushEvent(event);
                synchronized (queue) {
                    queue.notify();
                }
            }
        } else {
            synchronized (this) {
                final Set<Entry<String, MinTimeWeakReference<EventsAPIQueue>>> es = this.eventQueues.entrySet();
                final Iterator<Entry<String, MinTimeWeakReference<EventsAPIQueue>>> esi = es.iterator();
                while (esi.hasNext()) {
                    final Entry<String, MinTimeWeakReference<EventsAPIQueue>> next = esi.next();
                    final WeakReference<HttpSession> httpSession = this.sessionMap.get(next.getKey());
                    if (httpSession == null || httpSession.get() == null || httpSession.get().isAlive() == false) {
                        this.sessionMap.remove(next.getKey());
                        esi.remove();
                        continue;
                    } else {
                        EventsAPIQueue queue = null;
                        final MinTimeWeakReference<EventsAPIQueue> mqueue = next.getValue();
                        if (mqueue == null || (queue = mqueue.superget()) == null) {
                            /* we dont want to refresh mintimeweakreference */
                            this.sessionMap.remove(next.getKey());
                            esi.remove();
                            continue;
                        }
                        queue.pushEvent(event);
                        synchronized (queue) {
                            queue.notify();
                        }
                    }
                }
            }
        }
    }
}
