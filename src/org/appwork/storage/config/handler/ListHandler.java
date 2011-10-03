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


/**
 * @author Thomas
 *
 */
public abstract class ListHandler<T> extends KeyHandler<T> {

    /**
     * @param storageHandler
     * @param key
     */
    public ListHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
      
    }

}
