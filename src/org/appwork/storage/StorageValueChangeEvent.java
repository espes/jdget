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

/**
 * @author thomas
 * 
 */
public class StorageValueChangeEvent<E> extends StorageEvent {

    private final E oldValue;
    private final E newValue;
    private final String key;

    /**
     * @param storage
     * @param key
     * @param oldValue
     * @param newValue
     */
    public StorageValueChangeEvent(final Storage storage, final String key, final E oldValue, final E newValue) {
        super(storage);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * @return the newValue
     */
    public E getNewValue() {
        return this.newValue;
    }

    /**
     * @return the oldValue
     */
    public E getOldValue() {
        return this.oldValue;
    }

}
