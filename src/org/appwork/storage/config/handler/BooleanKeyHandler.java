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

import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultStringValue;

/**
 * @author Thomas
 * 
 */
public class BooleanKeyHandler extends KeyHandler<Boolean> {

    /**
     * @param storageHandler
     * @param key
     */
    public BooleanKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Boolean object) {

        this.storageHandler.putPrimitive(getKey(), object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void initHandler() {
       
    
    }
    @Override
    protected boolean initDefaults() throws Throwable {
        defaultValue=false;
       return super.initDefaults();
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Boolean object) throws Throwable {
        // TODO Auto-generated method stub

    }

  
    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultBooleanValue.class;
    }
    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Boolean getValue() {
   
      return primitiveStorage.get(getKey(), defaultValue);
        
     
    }

}
