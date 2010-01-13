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
 * this class is the interface for a database table.
 * 
 * @author $Author: unknown$
 * 
 */
abstract public class DatabaseInterfaceList {
    /**
     * The annotation defines the tablestructure
     */
    protected StorableAnnotation ann;
    /**
     * tableID is a unique id. e.g. the tablename
     */
    protected String tableID;

    public DatabaseInterfaceList(StorableAnnotation ann, String tableID) {

        this.ann = ann;
        this.tableID = tableID;
    }

    protected void setID(Storable stor, int id) {
        stor.setStorageID(id);

    }

    /**
     * Clears all data in this table.
     * 
     * @throws DBException
     */
    abstract public void clear() throws DBException;

    /**
     * Deletes the table
     * 
     * @throws DBException
     */
    abstract public void drop() throws DBException;

    /**
     * Add a STorable instance
     * 
     * @param account
     * @throws DBException
     */
    abstract public void add(Storable account) throws DBException;

    /**
     * Removes a storable Item
     * 
     * @param account
     * @throws DBException
     */
    abstract public void remove(Storable account) throws DBException;

    /**
     * Removes entry id
     * 
     * @param id
     * @throws DBException
     */
    abstract public void remove(int id) throws DBException;

    /**
     * Returns a list of all entries
     * 
     * @param <E>
     * @param inst
     * @return
     * @throws DBException
     */
    abstract public <E extends Storable> ArrayList<E> list(E inst) throws DBException;

    /**
     * Returns item with id throws exception if this item does not exist
     * 
     * @param <E>
     * @param id
     * @param inst
     * @return
     * @throws DBException
     */
    abstract public <E extends Storable> E get(int id, E inst) throws DBException;
}
