/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm.converter;

import java.sql.SQLException;

/**
 * @author coalado
 * 
 */
public class ClassClassConverter extends StringConverter {

    /**
     * @param db
     */
    public ClassClassConverter() {

    }

    @Override
    protected Object getValue(Object item) {
        // TODO Auto-generated method stub
        if (item == null) return null;
        return ((Class<?>) item).getName();
    }

    public Object get(Class<?> clazz, String where) throws SQLException {

        try {
            return Class.forName(super.get(clazz, where).toString());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
