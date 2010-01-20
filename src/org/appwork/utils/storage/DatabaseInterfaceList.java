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
abstract public class DatabaseInterfaceList<E extends Storable> extends ArrayList<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -1488793256769278977L;
    /**
     * tableID is a unique id. e.g. the tablename
     */
    protected String tableID;
    protected Class<E> clazz;
    protected StorableAnnotation ann;

    @SuppressWarnings("unchecked")
    public DatabaseInterfaceList(Class<? extends Storable> clazz2, String tableID) {
        this.clazz = (Class<E>) clazz2;
        ann = clazz2.getAnnotation(StorableAnnotation.class);
        this.tableID = tableID;
    }

    /**
     * Deletes the table
     * 
     * @throws DBException
     */
    abstract public void drop();

}
