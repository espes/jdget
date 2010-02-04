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


/**
 * @author coalado
 * 
 */
public class LongConverter extends PrimitiveWrapperClassConverter {

    private String type;

    /**
     * @param db
     */
    public LongConverter() {
        super("BIGINT");

        // TODO Auto-generated constructor stub
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
        if (item == null) return null;

        return ((Long) item).longValue();
    }

}
