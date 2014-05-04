/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.handler;

import java.lang.reflect.Type;

/**
 * @author Thomas
 * 
 */
public class ObjectKeyHandler extends ListHandler<Object> {

    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public ObjectKeyHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key, type);
    }

}
