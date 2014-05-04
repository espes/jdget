/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import org.appwork.utils.event.Eventsender;

/**
 * @author thomas
 * 
 */
public class ExtTableEventSender extends Eventsender<ExtTableListener, ExtTableEvent<?>> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.DefaultEvent)
     */
    @Override
    protected void fireEvent(final ExtTableListener listener, final ExtTableEvent<?> event) {
        listener.onExtTableEvent(event);
    }

}
