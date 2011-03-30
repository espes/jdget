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
public class StorageValueChangeEvent<E> extends StorageEvent<E> {

    /**
     * @param storage
     * @param key
     * @param oldValue
     * @param newValue
     */
    @SuppressWarnings("unchecked")
    public StorageValueChangeEvent(final Storage storage, final String key, final E oldValue, final E newValue) {
        super(storage, StorageEvent.Types.CHANGED, key, oldValue, newValue);

    }

    /**
     * @return the newValue
     */
    public E getNewValue() {
        return this.getParameter(1);
    }

    /**
     * @return the oldValue
     */
    public E getOldValue() {
        return this.getParameter(0);
    }

    /**
     * TODO: check
     * 
     * @return
     */
    public boolean hasChanged() {
        final E newV = this.getNewValue();
        final E oldV = this.getOldValue();
        if (oldV == null && newV != null) {
            return true;
        } else if (newV == null && oldV != null) {
            return true;
        } else if (newV != null) { return !newV.equals(oldV); }
        return false;
    }

}
