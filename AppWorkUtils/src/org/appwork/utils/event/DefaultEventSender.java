/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event;

/**
 * @author thomas
 * 
 */
public class DefaultEventSender<E extends DefaultEvent> extends Eventsender<DefaultEventListener<E>, E> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.Event)
     */
    @Override
    protected void fireEvent(final DefaultEventListener<E> listener, final E event) {
        listener.onEvent(event);

    }

}
