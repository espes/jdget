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

import org.appwork.exceptions.WTFException;
import org.appwork.storage.config.annotations.ConvertValueFrom;
import org.appwork.storage.config.annotations.JSonFieldName;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class Setter {

    private final String key;
    private final Method method;
    private final Type   type;
    private Class<?>     convertFromClass;

    /**
     * @param substring
     * @param m
     */
    public Setter(final String name, final Method m) {

        final JSonFieldName jsFieldName = m.getAnnotation(JSonFieldName.class);
        if (jsFieldName != null) {
            this.key = jsFieldName.value();
        } else {
            this.key = name;
        }
        final ConvertValueFrom convert = m.getAnnotation(ConvertValueFrom.class);
        if (convert != null) {
            this.convertFromClass = convert.value();
        }
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

        if (this.type instanceof Class && ((Class<?>) this.type).isEnum() && parameter != null) {
            parameter = Enum.valueOf((Class<Enum>) this.type, parameter + "");
        }

        if (this.convertFromClass != null && parameter != null && parameter.getClass().isAssignableFrom(this.convertFromClass)) {
            if (this.convertFromClass == String.class) {

                if (this.type == Byte.class || this.type == byte.class) {
                    parameter = Byte.parseByte((String) parameter);

                } else if (this.type == Character.class || this.type == char.class) {
                    parameter = (char) Byte.parseByte((String) parameter);

                } else if (this.type == Short.class || this.type == short.class) {
                    parameter = Short.parseShort((String) parameter);

                } else if (this.type == Integer.class || this.type == int.class) {
                    parameter = Integer.parseInt((String) parameter);
                } else if (this.type == Long.class || this.type == long.class) {
                    parameter = Long.parseLong((String) parameter);
                } else if (this.type == Float.class || this.type == float.class) {
                    parameter = Float.parseFloat((String) parameter);

                } else if (this.type == Double.class || this.type == double.class) {
                    parameter = Double.parseDouble((String) parameter);

                } else {
                    throw new WTFException("Unsupported Convert " + this.convertFromClass + " to " + this.getType());
                }

            } else {
                throw new WTFException("Unsupported Convert " + this.convertFromClass + " to " + this.getType());
            }

        }
        // System.out.println(this.key + " = " + parameter + " " + this.type);
        try {
            this.method.invoke(inst, parameter);
        } catch (final IllegalArgumentException e) {
            Log.L.severe(this.method + " " + parameter);
            throw e;
        }

    }
}
