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

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 * 
 */
public class IntegerKeyHandler extends KeyHandler<Integer> {

    private SpinnerValidator validator;

    /**
     * @param storageHandler
     * @param key
     */
    public IntegerKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Integer object) {
        this.storageHandler.putPrimitive(getKey(), object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {
     
        validator = getAnnotation(SpinnerValidator.class);
        if (validator != null) {
            min = (int) validator.min();
            max = (int) validator.max();

        }
    }

    private int min;
    private int max;
    @Override
    protected boolean initDefaults() throws Throwable {
        defaultValue=0;
       return super.initDefaults();
    }
    
    
    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultIntValue.class;
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {
        ArrayList<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
    
        list.add(SpinnerValidator.class);
        return (Class<? extends Annotation>[]) list.toArray(new Class<?>[] {}); }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Integer object) throws Throwable {
        if (validator != null) {
            int v = object.intValue();
            if (v < min || v > max) throw new ValidationException();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Integer getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}
