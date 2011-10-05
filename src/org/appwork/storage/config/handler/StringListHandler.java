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

import org.appwork.storage.config.annotations.DefaultStringArrayValue;

/**
 * @author Thomas
 * 
 */
public class StringListHandler extends ListHandler<String[]> {

    

    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public StringListHandler(StorageHandler<?> storageHandler, String key, Type type) {
        super(storageHandler, key, type);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {
        try {

            defaultValue = getAnnotation(DefaultStringArrayValue.class).value();

        } catch (NullPointerException e) {
        }
    }
    
    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultStringArrayValue.class;
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(String[] object) throws Throwable {
        // TODO Auto-generated method stub

    }   

}
