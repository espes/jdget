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
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 * 
 */
public class LongKeyHandler extends KeyHandler<Long> {

    private SpinnerValidator validator;
    private long             min;
    private long             max;

    /**
     * @param storageHandler
     * @param key
     */
    public LongKeyHandler(final StorageHandler<?> storageHandler, final String key) {
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

        return DefaultLongValue.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Long getValue() {
        return this.primitiveStorage.get(this.getKey(), this.defaultValue);
    }

    @Override
    protected void initDefaults() throws Throwable {
        this.defaultValue = 0l;
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
            this.min = this.validator.min();
            this.max = this.validator.max();

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(final Long object) {

        this.storageHandler.putPrimitive(this.getKey(), object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final Long object) throws Throwable {
        if (this.validator != null) {
            final long v = object.longValue();
            if (v < this.min || v > this.max) { throw new ValidationException(); }
        }
    }

}
