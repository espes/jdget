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

import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.event.SimpleEvent;

/**
 * @author thomas
 * 
 */
public class ConfigEvent extends SimpleEvent<KeyHandler<?>, Object, ConfigEvent.Types> {
    public static enum Types {
        VALUE_UPDATED,
        /**
         * Parameter[0] = Throwable from value validator<br>
         * Parameter[1] = Methodhandler
         */
        VALIDATOR_ERROR

    }

    /**
     * @param caller
     * @param type
     * @param parameters
     */
    public ConfigEvent( final Types type, KeyHandler<?> caller,final Object parameter) {
        super(caller, type, parameter);
        // TODO Auto-generated constructor stub
    }

}
