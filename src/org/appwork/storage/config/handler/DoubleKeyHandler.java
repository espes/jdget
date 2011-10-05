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
import org.appwork.storage.config.annotations.DefaultDoubleValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 *
 */
public class DoubleKeyHandler extends KeyHandler<Double> {

    /**
     * @param storageHandler
     * @param key
     */
    public  DoubleKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Double object) {
        this.storageHandler.putPrimitive(getKey(),  object);
    }
    
    protected  Class<? extends Annotation> getDefaultAnnotation(){
      return  DefaultDoubleValue.class;
    }
    
    
    
    @Override
    protected boolean initDefaults() throws Throwable {
        defaultValue=0d;
       return super.initDefaults();
    }


    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Double object) throws Throwable {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Double getValue() {
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
