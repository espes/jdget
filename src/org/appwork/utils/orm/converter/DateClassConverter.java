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
import java.util.Date;

/**
 * @author thomas
 * 
 */
public class DateClassConverter extends StringConverter {

    /**
     * @param db
     */
    public DateClassConverter() {

        // TODO Auto-generated constructor stub
    }

    @Override
    protected Object getValue(Object item) {
        // TODO Auto-generated method stub
        if (item == null) return null;
        return ((Date) item).getTime();
    }

    public Object get(Class<?> clazz, String where) throws SQLException {

        return new Date(Long.parseLong((String) super.get(clazz, where)));

    }

}
