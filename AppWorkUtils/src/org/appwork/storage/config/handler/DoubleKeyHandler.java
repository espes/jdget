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

import org.appwork.storage.config.annotations.DefaultDoubleValue;

/**
 * @author Thomas
 * 
 */
public class DoubleKeyHandler extends KeyHandler<Double> {

    /**
     * @param storageHandler
     * @param key
     */
    public DoubleKeyHandler(final StorageHandler<?> storageHandler, final String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {
        return DefaultDoubleValue.class;
    }

    @Override
    protected void initDefaults() throws Throwable {
        this.setDefaultValue(0d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.handler.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() throws Throwable {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(final Double object) {
        this.storageHandler.getPrimitiveStorage().put(this.getKey(), object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final Double object) throws Throwable {
        // TODO Auto-generated method stub

    }

}
