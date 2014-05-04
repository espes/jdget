/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.appwork.storage.simplejson.mapper.CompiledTypeRef;

/**
 * @author thomas
 * 
 */
public abstract class TypeRef<T> {

    private final Type type;

    public TypeRef() {
        final Type superClass = this.getClass().getGenericSuperclass();

        if (superClass instanceof Class) { throw new IllegalArgumentException("Wrong TypeRef Construct"); }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

    }

    public TypeRef(final Type t) {

        this.type = t;

    }

    public Type getType() {
        return this.type;
    }

    /**
     * @return
     */
    public CompiledTypeRef compile() {
     final CompiledTypeRef ret = new CompiledTypeRef(getType());
        return ret;
    }

}
