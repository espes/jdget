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
import java.util.ArrayList;

import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.annotations.DefaultStringValue;

/**
 * @author Thomas
 * 
 */
public class StringKeyHandler extends KeyHandler<String> {

    /**
     * @param storageHandler
     * @param key
     */
    public StringKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */


    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultStringValue.class;
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(String object) throws Throwable {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(String object) {
        primitiveStorage.put(getKey(), object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public String getValue() {

        return primitiveStorage.get(getKey(), defaultValue);
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() throws Throwable {
        // TODO Auto-generated method stub
        
    }

}
