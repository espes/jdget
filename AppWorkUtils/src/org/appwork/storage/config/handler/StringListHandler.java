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
import java.lang.reflect.Type;

import org.appwork.storage.config.annotations.DefaultStringArrayValue;

/**
 * @author Thomas
 * 
 */
public class StringListHandler extends ListHandler<String[]> {

    /**
     * @param storageHandler
     * @param key
     * @param type
     */
    public StringListHandler(final StorageHandler<?> storageHandler, final String key, final Type type) {
        super(storageHandler, key, type);
    }

    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {
        return DefaultStringArrayValue.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(final String[] object) throws Throwable {
    }

}
