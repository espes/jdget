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
 * @author daniel
 * 
 */
public class StorageKeyRemovedEvent<E> extends StorageEvent<E> {

    @SuppressWarnings("unchecked")
    public StorageKeyRemovedEvent(final Storage storage, final String key, final E oldValue) {
        super(storage, StorageEvent.Types.REMOVED, key, oldValue);

    }

    /**
     * @return the newValue
     */
    public E getOldValue() {
        return this.getParameter(0);
    }
}
