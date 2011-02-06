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

import org.codehaus.jackson.type.TypeReference;

/**
 * @author thomas
 * 
 */
public class TypeRef extends TypeReference<Object> {

    private final Type type;

    /**
     * @param type
     */
    public TypeRef(final java.lang.reflect.Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

}
