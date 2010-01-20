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

/**
 * @author $Author: unknown$
 * 
 */
public interface DatabaseInterface {

    /**
     * Save data in Database
     * 
     * @param key
     * @param value
     */
    void put(String key, Object value) throws DBException;

    /**
     * Get a saved value from database
     * 
     * @param <E>
     * @param key
     * @param def
     *            Generic defaulvalue. must be of the desired return type
     * @return
     * @throws Exception
     */
    public <E> E get(String key, E def) throws DBException;

}
