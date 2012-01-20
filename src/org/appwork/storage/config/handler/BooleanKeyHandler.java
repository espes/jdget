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

import org.appwork.storage.config.annotations.DefaultBooleanValue;

/**
 * @author Thomas
 * 
 */
public class BooleanKeyHandler extends KeyHandler<Boolean> {

    /**
     * @param storageHandler
     * @param key
     */
    public BooleanKeyHandler(final StorageHandler<?> storageHandler, final String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultBooleanValue.class;
    }

    @Override
    protected void initDefaults() throws Throwable {
        this.setDefaultValue(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */

    @Override
    protected void initHandler() {

    }

    public boolean isEnabled() {
        final Boolean value = this.getValue();
        if (value == null || value == false) { return false; }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(final Boolean object) {
        this.storageHandler.putPrimitive(this.getKey(), object);
    }

    public void toggle() {
        this.setValue(!this.getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final Boolean object) throws Throwable {
        // TODO Auto-generated method stub

    }

}
