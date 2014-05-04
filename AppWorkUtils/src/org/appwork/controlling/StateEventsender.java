/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.controlling
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.controlling;

import org.appwork.utils.event.Eventsender;

/**
 * @author thomas
 * 
 */
public class StateEventsender extends Eventsender<StateEventListener, StateEvent> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.Event)
     */
    @Override
    protected void fireEvent(final StateEventListener listener, final StateEvent event) {
        switch (event.getType()) {
        case CHANGED:
            listener.onStateChange(event);
            break;
        case UPDATED:
            listener.onStateUpdate(event);
            break;
        }

    }

}