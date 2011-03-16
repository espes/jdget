/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.lang.reflect.Type;

/**
 * @author thomas
 * 
 */
public class ConfigTypeReference implements org.appwork.storage.TypeRef<Object> {

    private final Type type;

    /**
     * @param type
     */
    public ConfigTypeReference(final java.lang.reflect.Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

}
