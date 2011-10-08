/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultEnumArrayValue;
import org.appwork.storage.config.annotations.DefaultStringArrayValue;

/**
 * @author Thomas
 *
 */
public class EnumListHandler extends ListHandler<Enum<?>[]> {



    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public EnumListHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key, type);
        // TODO Auto-generated constructor stub
    }



    
    @Override
    protected boolean initDefaults() throws Throwable {
        final DefaultEnumArrayValue ann = this.getAnnotation(DefaultEnumArrayValue.class);
         
        if (ann != null) {
        
            // chek if this is really the best way to convert string
            // to
            // enum
            final Enum[] ret = new Enum[ann.value().length];
            for (int i = 0; i < ret.length; i++) {
                final int index = ann.value()[i].lastIndexOf(".");
                final String name = ann.value()[i].substring(index + 1);
                final String clazz = ann.value()[i].substring(0, index);

                ret[i] = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);
            }
        defaultValue=ret;
        return true;

        }
        defaultValue=  null;
        return false;
    }




    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultEnumArrayValue.class;
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Enum<?>[] object) throws Throwable {
        // TODO Auto-generated method stub

    }

}
