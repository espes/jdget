/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class Setter {

    private final String key;
    private final Method method;
    private final Type   type;

    /**
     * @param substring
     * @param m
     */
    public Setter(final String name, final Method m) {
        this.key = name;
        this.method = m;
        m.setAccessible(true);
        this.type = m.getGenericParameterTypes()[0];
    }

    public String getKey() {
        return this.key;
    }

    public Method getMethod() {
        return this.method;
    }

    public Type getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    public void setValue(final Object inst, Object parameter) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        // if (parameter instanceof Number) {
        // if (this.type == Byte.class || this.type == byte.class) {
        // parameter = ((Long) parameter).byteValue();
        //
        // } else if (this.type == Character.class || this.type == char.class) {
        // parameter = (char) ((Long) parameter).byteValue();
        //
        // } else if (this.type == Short.class || this.type == short.class) {
        // parameter = ((Long) parameter).shortValue();
        //
        // } else if (this.type == Integer.class || this.type == int.class) {
        // parameter = ((Long) parameter).intValue();
        // } else if (this.type == Long.class || this.type == long.class) {
        // parameter = ((Long) parameter).longValue();
        // } else if (this.type == Float.class || this.type == float.class) {
        // parameter = ((Double) parameter).floatValue();
        //
        // } else if (this.type == Double.class || this.type == double.class) {
        // parameter = ((Double) parameter).doubleValue();
        //
        // }
        // }
        if (this.type instanceof Class && ((Class<?>) this.type).isEnum()) {
            parameter = Enum.valueOf((Class<Enum>) this.type, parameter + "");
        }
        // System.out.println(this.key + " = " + parameter + " " + this.type);
        try {
            this.method.invoke(inst, parameter);
        } catch (IllegalArgumentException e) {
            Log.L.severe(method + " " + parameter);
            throw e;
        }

    }
}
