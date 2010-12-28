/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage;

import org.appwork.utils.event.SimpleEvent;

/**
 * @author thomas
 * @param <E>
 * 
 */
public class StorageEvent<E> extends SimpleEvent<Storage, E, StorageEvent.Types> {

    /**
     * @author thomas
     * 
     */
    public enum Types {
        ADDED,
        CHANGED,
        REMOVED
    }

    private final String key;

    /**
     * @param caller
     * @param type
     * @param parameters
     */
    public StorageEvent(final Storage caller, final StorageEvent.Types type, final String key, final E... parameters) {
        super(caller, type, parameters);
        this.key = key;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @param jacksonStorageChest
     * @param changed
     * @param key
     * @param value
     * @param object
     */

    /**
     * @param storage
     */
    // protected StorageEvent(final Storage storage) {
    // super(storage, 0);
    // }

}
