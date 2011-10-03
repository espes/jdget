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

import org.appwork.storage.config.ValidationException;
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
        try {
            this.defaultValue = getAnnotation(DefaultIntValue.class).value();
        } catch (NullPointerException e) {
        }
        validator = getAnnotation(SpinnerValidator.class);
        if (validator != null) {
            min = (int) validator.min();
            max = (int) validator.max();

        }
    }

    private int min;
    private int max;

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {
        return (Class<? extends Annotation>[]) new Class<?>[] { DefaultIntValue.class, SpinnerValidator.class };
    }

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
    protected Integer getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}
