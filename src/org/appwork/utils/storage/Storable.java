/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.storage;

import java.util.ArrayList;

/**
 * Use this interface if you want to stiore instances of a certain class to
 * databse. The class can be saved to a DatabaseInterfaceList if it implements
 * Storable and has a STorableAnnotation. STorableAnnotation defines the
 * required fields and datatypes
 * 
 * @author $Author: unknown$
 * 
 */
public interface Storable {
    /**
     * Is called by the databaseinterface to store the instance. It is called
     * for each name-type pair in the STorableAnnotation
     * 
     * @param id
     *            the id ... STorableAnnotation.indexof(key)
     * @param key
     *            given by the STorableAnnotation.names()
     * @param clazz
     *            given by the STorableAnnotation.types()
     * @return
     */
    public Object getStore(int id, String key, Class<?> clazz);

    /**
     * TRhis method must restore a fresh instance of Storable.
     * 
     * @param data
     *            [UNIQUE INT ID,HASH INT,..data according to
     *            STorableAnnotation]
     */
    public void restore(ArrayList<Object> data);

    /**
     * The storageID should be set by Databaseinterface after restoring the
     * instance
     * 
     * @param id
     */
    public void setStorageID(int id);
}
