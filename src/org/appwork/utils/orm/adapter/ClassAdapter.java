/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm.adapter
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm.adapter;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.appwork.utils.orm.ORMapper;

/**
 * @author thomas
 * 
 */
abstract public class ClassAdapter {

    protected ORMapper owner;

    /**
     * @param clazz
     * @return
     */
    abstract public HashMap<String, Field> getDeclaredFields(Class<?> clazz);

    /**
     * @param ca
     */
    public void setOwner(ORMapper owner) {
        this.owner = owner;

    }
}
