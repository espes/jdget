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

import org.appwork.utils.event.DefaultEvent;

/**
 * @author thomas
 * 
 */
public class StorageEvent extends DefaultEvent {

    /**
     * @param jacksonStorageChest
     * @param put2
     * @param boolean1
     * @param value
     * @return
     */
    public static <E> StorageEvent createChangeEvent(final Storage storage, final String key, final E oldValue, final E newValue) {
        if (oldValue == newValue) { return null; }
        if (oldValue != null && oldValue.equals(newValue)) { return null; }

        return new StorageValueChangeEvent<E>(storage, key, oldValue, newValue);
    }

    /**
     * @param storage
     */
    protected StorageEvent(final Storage storage) {
        super(storage, 0);
    }

}
