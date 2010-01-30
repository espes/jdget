/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm;

import java.sql.Connection;

/**
 * @author coalado
 * 
 */
public class StringConverter extends PrimitiveWrapperClassConverter {

    /**
     * @param db
     */
    public StringConverter(Connection db) {
        super(db, "LONGVARCHAR");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.orm.PrimitiveWrapperClassConverter#getValue(java.lang
     * .Object)
     */
    @Override
    protected Object getValue(Object item) {
        // TODO Auto-generated method stub
        if (item == null) return null;
        return item.toString();
    }

}
