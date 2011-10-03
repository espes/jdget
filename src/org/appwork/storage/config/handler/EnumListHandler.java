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

import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultEnumArrayValue;

/**
 * @author Thomas
 *
 */
public class EnumListHandler extends ListHandler<Enum<?>[]> {

    /**
     * @param storageHandler
     * @param key
     */
    public EnumListHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() throws Throwable {
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

        }
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {       
        return (Class<? extends Annotation>[]) new Class<?>[]{DefaultEnumArrayValue.class};
    }
    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Enum<?>[] object) throws Throwable {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Enum<?>[] object) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    protected Enum<?>[] getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}
