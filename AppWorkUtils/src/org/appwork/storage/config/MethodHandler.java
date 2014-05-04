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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.appwork.storage.config.handler.StorageHandler;

/**
 * @author thomas
 * 
 */
public class MethodHandler {

    public static enum Type {
        GETTER,
        SETTER;
    }

    private final Type             type;
    private final String           key;
    private final Method           method;

    private Class<?>               rawClass;
    private java.lang.reflect.Type rawType;

    /**
     * @param getter
     * @param key
     * @param m
     * @param canStorePrimitive
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     */
    public MethodHandler(final StorageHandler<?> storageHandler, final Type getter, final String key, final Method m) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        type = getter;
        this.key = key;
        method = m;

        if (isGetter()) {

            rawClass = m.getReturnType();
            rawType = m.getGenericReturnType();
        } else {

            rawClass = m.getParameterTypes()[0];
            rawType = m.getGenericParameterTypes()[0];
        }

    }

    public String getKey() {
        return key;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getRawClass() {
        return rawClass;
    }

    public java.lang.reflect.Type getRawType() {
        return rawType;
    }

    public Type getType() {
        return type;
    }

    /**
     * @return
     */
    public boolean isGetter() {

        return type == Type.GETTER;
    }

    @Override
    public String toString() {
        return method + "";
    }

}
