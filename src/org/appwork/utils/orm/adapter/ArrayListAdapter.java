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
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * @author thomas
 * 
 */
public class ArrayListAdapter extends ClassAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.orm.adapter.ClassAdapter#getDeclaredFields(java.lang
     * .Class)
     */
    @Override
    public HashMap<String, Field> getDeclaredFields(Class<?> clazz) {
        HashMap<String, Field> ret = new HashMap<String, Field>();
        Field[] fields = owner.getFields(clazz);
        for (Field f : fields) {

            // no statics
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            ret.put(f.getName(), f);

        }
        return ret;
    }

}
