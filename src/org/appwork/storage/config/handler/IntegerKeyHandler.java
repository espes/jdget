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
import org.appwork.storage.config.annotations.DefaultIntValue;
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
        final ArrayList<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();

        list.add(SpinnerValidator.class);
        return (Class<? extends Annotation>[]) list.toArray(new Class<?>[] {});
    }

    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultIntValue.class;
    }

    @Override
    protected void initDefaults() throws Throwable {
        this.setDefaultValue(Integer.valueOf(0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {

        this.validator = this.getAnnotation(SpinnerValidator.class);
        if (this.validator != null) {
            this.min = (int) this.validator.min();
            this.max = (int) this.validator.max();

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(final Integer object) {
        this.storageHandler.putPrimitive(this.getKey(), object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final Integer object) throws Throwable {
        if (this.validator != null) {
            final int v = object.intValue();
            if (v < this.min || v > this.max) { throw new ValidationException(); }
        }

    }

}
