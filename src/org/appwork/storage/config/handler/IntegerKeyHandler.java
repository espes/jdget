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
import org.appwork.storage.config.annotations.LookUpKeys;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 * 
 */
public class IntegerKeyHandler extends KeyHandler<Integer> {

    private SpinnerValidator validator;

    private int              min;

    private int              max;

    /**
     * @param storageHandler
     * @param key
     */
    public IntegerKeyHandler(final StorageHandler<?> storageHandler, final String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getAllowedAnnotations() {
        return (Class<? extends Annotation>[]) new Class<?>[] {LookUpKeys.class,SpinnerValidator.class};
    }

    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultIntValue.class;
    }

    @Override
    protected void initDefaults() throws Throwable {
        setDefaultValue(Integer.valueOf(0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {

        validator = this.getAnnotation(SpinnerValidator.class);
        if (validator != null) {
            min = (int) validator.min();
            max = (int) validator.max();

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(final Integer object) {
        storageHandler.putPrimitive(getKey(), object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final Integer object) throws Throwable {
        if (validator != null) {
            final int v = object.intValue();
            if (v < min || v > max) { throw new ValidationException(); }
        }

    }

}
