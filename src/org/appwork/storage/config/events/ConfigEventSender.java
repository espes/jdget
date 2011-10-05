/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.events;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.event.Eventsender;

/**
 * @author thomas
 * 
 */
public class ConfigEventSender<RawClass> extends Eventsender<GenericConfigEventListener<RawClass>, ConfigEvent> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.DefaultEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void fireEvent(final GenericConfigEventListener<RawClass> listener, final ConfigEvent event) {
        switch (event.getType()) {
        case VALUE_UPDATED:
            listener.onConfigValueModified((KeyHandler<RawClass>)event.getCaller(), (RawClass)event.getParameter());
            break;
        case VALIDATOR_ERROR:
           
            listener.onConfigValidatorError((KeyHandler<RawClass>)event.getCaller(), (RawClass) ((ValidationException) event.getParameter()).getValue(), (ValidationException) event.getParameter());
            break;
        default:
            throw new RuntimeException(event.getType() + " is not handled");
        }

    }

}
