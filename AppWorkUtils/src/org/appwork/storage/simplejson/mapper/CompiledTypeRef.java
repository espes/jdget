/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;

/**
 * @author Thomas
 * 
 */
public class CompiledTypeRef {

    private Type     type;
    private Class<?> rawType;

    public Type getType() {
        return type;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public boolean hasSubTypes() {
        return subTypes != null && subTypes.length > 0;
    }

    public CompiledTypeRef[] getSubTypes() {
        return subTypes;
    }

    private CompiledTypeRef[] subTypes;

    /**
     * @param type
     */
    @SuppressWarnings("rawtypes")
    public CompiledTypeRef(final Type type) {
        this.type = type;

        if (type instanceof ParameterizedType) {
            rawType = (Class<?>)((ParameterizedType) type).getRawType();
            final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            subTypes = new CompiledTypeRef[types.length];
            for (int i = 0; i < types.length; i++) {
                subTypes[i] = new CompiledTypeRef(types[i]);
            }

        } else if (type instanceof Class) {
            rawType = (Class) type;
        } else if (type instanceof GenericArrayType) {
            // this is for 1.6
            // for 1.7 we do not get GenericArrayTypeImpl here but the actual
            // array class
            rawType = Array.newInstance((Class<?>) ((GenericArrayType) type).getGenericComponentType(), 0).getClass();
        }

    }
}
