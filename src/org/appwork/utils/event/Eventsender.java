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

    /**
     * List of registered Eventlistener
     */
    // TODO: DO we really need Vectors here?
    transient protected Vector<T> listeners = null;
    /**
     * List of Listeners that are requested for removal
     * 
     */
    // We use a removeList to avoid threating problems
    transient protected Vector<T> removeRequestedListeners = null;

    /**
     * Creates a new Eventsender Instance
     */
    public Eventsender() {
        listeners = new Vector<T>();
        removeRequestedListeners = new Vector<T>();
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
    public void addListener(T listener) {
        if (removeRequestedListeners.contains(listener)) removeRequestedListeners.remove(listener);
        if (!listeners.contains(listener)) listeners.add(listener);
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
    // synchronized to avoid parallel runs
    public void fireEvent(TT event) {
        // first handle removelist
        synchronized (removeRequestedListeners) {
            listeners.removeAll(removeRequestedListeners);
            removeRequestedListeners.clear();
        }
        // then run through residual listeners
        for (int i = listeners.size() - 1; i >= 0; i--) {
            this.fireEvent(listeners.get(i), event);
        }
        // clean up listenerslist again. maybe there are new entries in removal
        // list
        if (removeRequestedListeners.size() > 0) {
            synchronized (removeRequestedListeners) {
                listeners.removeAll(removeRequestedListeners);
                removeRequestedListeners.clear();
            }
        }

    }

    public void removeListener(T listener) {
        if (!removeRequestedListeners.contains(listener)) removeRequestedListeners.add(listener);
    }
}
