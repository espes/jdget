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
 */
public class StorageKeyAddedEvent<E> extends StorageEvent<E> {

    @SuppressWarnings("unchecked")
    public StorageKeyAddedEvent(final Storage storage, final String key, final E newValue) {
        super(storage, StorageEvent.Types.ADDED, key, newValue);
    }

    /**
     * @return the newValue
     */
    public E getNewValue() {
        return this.getParameter(0);
    }

}
