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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.appwork.storage.StorageException;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * 
 */
public class KeyHandler {

    private static final int             MIN_LIFETIME = 10000;
    private final String                 key;
    private MethodHandler                getter;
    private MethodHandler                setter;
    private final StorageHandler<?>      storageHandler;

    private MinTimeWeakReference<Object> cache;

    /**
     * @param storageHandler
     * @param key2
     */
    public KeyHandler(final StorageHandler<?> storageHandler, final String key) {
        this.storageHandler = storageHandler;
        this.key = key;
        // this.refQueue = new ReferenceQueue<Object>();

    }

    /**
     * @param <T>
     * @param class1
     * @return
     */
    public <T extends Annotation> T getAnnotation(final Class<T> class1) {
        T ret = this.getter.getClass().getAnnotation(class1);
        if (ret == null) {
            ret = this.setter.getClass().getAnnotation(class1);
        }
        return ret;
    }

    /**
     * @return
     */
    public Object getDefaultValue() {
        if (this.isPrimitive()) {
            if (Clazz.isBoolean(this.getter.getRawClass())) {
                return this.getter.getDefaultBoolean();
            } else if (Clazz.isLong(this.getter.getRawClass())) {
                return this.getter.getDefaultLong();
            } else if (Clazz.isInteger(this.getter.getRawClass())) {
                return this.getter.getDefaultInteger();
            } else if (Clazz.isByte(this.getter.getRawClass())) {
                return this.getter.getDefaultByte();
            } else if (Clazz.isFloat(this.getter.getRawClass())) {
                return this.getter.getDefaultFloat();
            } else if (this.getter.getRawClass() == String.class) {
                return this.getter.getDefaultString();
            } else if (this.getter.getRawClass().isEnum()) {

                return this.getter.getDefaultEnum();
            } else if (Clazz.isDouble(this.getter.getRawClass())) {
                return this.getter.getDefaultDouble();
            } else {
                return null;
            }
        } else {
            return this.getter.getDefaultObject();

        }

    }

    public MethodHandler getGetter() {
        return this.getter;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * @return
     */
    public Class<?> getRawClass() {
        // TODO Auto-generated method stub
        return this.getter.getRawClass();
    }

    public MethodHandler getSetter() {
        return this.setter;
    }

    public StorageHandler<?> getStorageHandler() {
        return this.storageHandler;
    }

    /**
     * @return
     */
    public Object getValue() {

        if (this.getter.isPrimitive()) {

            if (this.getter.getRawClass() == Boolean.class || this.getter.getRawClass() == boolean.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultBoolean());
            } else if (this.getter.getRawClass() == Long.class || this.getter.getRawClass() == long.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultLong());
            } else if (this.getter.getRawClass() == Integer.class || this.getter.getRawClass() == int.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultInteger());
            } else if (this.getter.getRawClass() == Float.class || this.getter.getRawClass() == float.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultFloat());
            } else if (this.getter.getRawClass() == Byte.class || this.getter.getRawClass() == byte.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultByte());
            } else if (this.getter.getRawClass() == String.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultString());
                // } else if (getter.getRawClass() == String[].class) {
                // return this.storageHandler.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (this.getter.getRawClass().isEnum()) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultEnum());
            } else if (this.getter.getRawClass() == Double.class | this.getter.getRawClass() == double.class) {
                return this.storageHandler.getPrimitive(this.getter.getKey(), this.getter.getDefaultDouble());
            } else {
                throw new StorageException("Invalid datatype: " + this.getter.getRawClass());
            }

        } else {
            Object ret = this.cache != null ? this.cache.get() : null;
            if (ret == null) {
                ret = this.getter.read();

                this.cache = new MinTimeWeakReference<Object>(ret, KeyHandler.MIN_LIFETIME);

            }
            return ret;
        }
    }

    /**
     * @param m
     * @return
     */
    public boolean isGetter(final Method m) {

        return m.equals(this.getter.getMethod());
    }

    /**
     * @return
     */
    public boolean isPrimitive() {
        return this.getter.isPrimitive();
    }

    /**
     * @param h
     */
    public void setGetter(final MethodHandler h) {
        this.getter = h;
        if (this.setter != null) {
            this.validDateCrypt();
        }
    }

    /**
     * @param h
     */
    public void setSetter(final MethodHandler h) {
        this.setter = h;
        if (this.getter != null) {
            this.validDateCrypt();
        }
    }

    /**
     * @param object
     */
    public void setValue(final Object object) {
        if (this.setter.isPrimitive()) {

            if (this.setter.getRawClass() == Boolean.class || this.setter.getRawClass() == boolean.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Boolean) object);

            } else if (this.setter.getRawClass() == Long.class || this.setter.getRawClass() == long.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Long) object);
            } else if (this.setter.getRawClass() == Integer.class || this.setter.getRawClass() == int.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Integer) object);

            } else if (this.setter.getRawClass() == Float.class || this.setter.getRawClass() == float.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Float) object);
            } else if (this.setter.getRawClass() == Byte.class || this.setter.getRawClass() == byte.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Byte) object);
            } else if (this.setter.getRawClass() == String.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (String) object);
            } else if (this.setter.getRawClass().isEnum()) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Enum<?>) object);
            } else if (this.setter.getRawClass() == Double.class || this.setter.getRawClass() == double.class) {
                this.storageHandler.putPrimitive(this.setter.getKey(), (Double) object);
            } else {
                throw new StorageException("Invalid datatype: " + this.setter.getRawClass());
            }

        } else {
            this.setter.write(object);
            this.cache = new MinTimeWeakReference<Object>(object, KeyHandler.MIN_LIFETIME);

        }
    }

    /**
     * checks wether crypt Annotations in getter equal cryptsettings in setter
     */
    private void validDateCrypt() {
        if (this.getter.isCrypted() != this.setter.isCrypted()) { throw new InterfaceParseException(this.getter + " cryptsettings != " + this.setter); }// check
        // keys
        if (this.getter.isCrypted()) {
            if (!Arrays.equals(this.getter.getCryptKey(), this.setter.getCryptKey())) { throw new InterfaceParseException(this.getter + " cryptkey mismatch" + this.setter); }

        }
    }
}
