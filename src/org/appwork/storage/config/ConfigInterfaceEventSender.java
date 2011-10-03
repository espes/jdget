/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.event.Eventsender;

/**
 * @author thomas
 * 
 */
public class ConfigInterfaceEventSender<T extends ConfigInterface> extends Eventsender<ConfigEventListener, ConfigEvent<T>> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.event.Eventsender#fireEvent(java.util.EventListener,
     * org.appwork.utils.event.DefaultEvent)
     */
    @Override
    protected void fireEvent(final ConfigEventListener listener, final ConfigEvent<T> event) {
        switch (event.getType()) {
        case VALUE_UPDATED:
            listener.onConfigValueModified(event.getCaller(), (String) event.getParameter(0), event.getParameter(1));
            break;
        case VALIDATOR_ERROR:
            listener.onConfigValidatorError(event.getCaller(), (Throwable) event.getParameter(0), (KeyHandler) event.getParameter(1));
            break;
        default:
            throw new RuntimeException(event.getType() + " is not handled");
        }

    }

}
