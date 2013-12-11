/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.circlebar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.predefined.changeevent;

import org.appwork.utils.event.Eventsender;

/**
 * @author thomas
 * 
 */
public class ChangeEventSender extends Eventsender<ChangeListener, ChangeEvent> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.DefaultEvent)
     */
    @Override
    protected void fireEvent(final ChangeListener listener, final ChangeEvent event) {
        listener.onChangeEvent(event);
    }
}
