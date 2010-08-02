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

import java.util.EventListener;
import java.util.Vector;

/**
 * The Eventsenderclass is the core of the Eventsystem. it can be used to design
 * new Eventbroadcaster Systems easily.
 * 
 * 
 * 
 * @author $Author: unknown$
 * 
 */

public abstract class Eventsender<T extends EventListener, TT extends Event> {

    private final Object LOCK = new Object();
    private volatile long WriteR = 0;
    private volatile long ReadR = 0;
    /**
     * List of registered Eventlistener
     */
    // TODO: DO we really need Vectors here?
    transient volatile protected Vector<T> listeners = null;
    /**
     * List of Listeners that are requested for removal
     * 
     */
    // We use a removeList to avoid threating problems
    transient protected Vector<T> removeRequestedListeners = null;

    transient protected Vector<T> addRequestedListeners = null;

    /**
     * Creates a new Eventsender Instance
     */
    public Eventsender() {
        listeners = new Vector<T>();
        removeRequestedListeners = new Vector<T>();
        addRequestedListeners = new Vector<T>();
    }

    /**
     * Adds a list of listeners
     * 
     * @param listener
     */
    public void addAllListener(Vector<T> listener) {
        for (T l : listener)
            this.addListener(l);

    }

    /**
     * Add a single Listener
     * 
     * @param listener
     */
    public void addListener(T t) {
        synchronized (LOCK) {
            /* decrease WriteCounter in case we remove the removeRequested */
            if (removeRequestedListeners.contains(t)) {
                removeRequestedListeners.remove(t);
                WriteR--;
            }
            /*
             * increase WriteCounter in case we add addRequestedListeners and t
             * is not in current listeners list
             */
            if (!addRequestedListeners.contains(t) && !listeners.contains(t)) {
                addRequestedListeners.add(t);
                WriteR++;
            }
        }
    }

    /**
     * Abstract fire Event Method.
     * 
     * @param listener
     * @param event
     */
    protected abstract void fireEvent(T listener, TT event);

    /**
     * Fires an Event to all registered Listeners
     * 
     * @param event
     * @return
     */

    public void fireEvent(TT event) {
        Vector<T> listeners;
        synchronized (LOCK) {
            if (WriteR == ReadR) {
                /* nothing changed, we can use old pointer to listeners */
                if (this.listeners.size() == 0) return;
                listeners = this.listeners;
            } else {
                /* create new list with copy of old one */
                listeners = new Vector<T>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(removeRequestedListeners);
                removeRequestedListeners.clear();
                listeners.addAll(addRequestedListeners);
                addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                ReadR = WriteR;
                this.listeners = listeners;
                if (this.listeners.size() == 0) return;
            }
        }
        for (T t : listeners) {
            this.fireEvent(t, event);
        }
        synchronized (LOCK) {
            if (WriteR != ReadR) {
                /* something changed, lets update the list */
                /* create new list with copy of old one */
                listeners = new Vector<T>(this.listeners);
                /* remove and add wished items */
                listeners.removeAll(removeRequestedListeners);
                removeRequestedListeners.clear();
                listeners.addAll(addRequestedListeners);
                addRequestedListeners.clear();
                /* update ReadCounter and pointer to listeners */
                ReadR = WriteR;
                this.listeners = listeners;
            }
        }
    }

    public void removeListener(T t) {
        synchronized (LOCK) {
            /* decrease WriteCounter in case we remove the addRequest */
            if (addRequestedListeners.contains(t)) {
                addRequestedListeners.remove(t);
                WriteR--;
            }
            /*
             * increase WriteCounter in case we add removeRequest and t is in
             * current listeners list
             */
            if (!removeRequestedListeners.contains(t) && listeners.contains(t)) {
                removeRequestedListeners.add(t);
                WriteR++;
            }
        }
    }
}
