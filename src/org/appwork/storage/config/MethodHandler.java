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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storable;
import org.appwork.storage.StorageException;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultBooleanArrayValue;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteArrayValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultDoubleArrayValue;
import org.appwork.storage.config.annotations.DefaultDoubleValue;
import org.appwork.storage.config.annotations.DefaultEnumArrayValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultFloatArrayValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultIntArrayValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultLongArrayValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultObjectValue;
import org.appwork.storage.config.annotations.DefaultStringArrayValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.annotations.PlainStorage;
import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class MethodHandler {

    public static enum Type {
        GETTER,
        SETTER;
    }

    private final Type    type;
    private final String  key;
    private final Method  method;

    private final boolean primitive;
    private Class<?>      rawClass;
    private boolean       defaultBoolean     = false;
    private long          defaultLong        = 0l;
    private int           defaultInteger     = 0;
    private Enum<?>       defaultEnum        = null;
    private double        defaultDouble      = 0.0d;
    private String        defaultString      = null;
    private String[]      defaultStringArray = null;
    private byte          defaultByte        = 0;
    private Object        defaultObject      = null;
    private float         defaultFloat       = 0.0f;
    private MethodHandler getterHandler;
    private MethodHandler setterHandler;
    private boolean       crypted;
    private byte[]        cryptKey;

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
    @SuppressWarnings( { "unchecked", "rawtypes" })
    public MethodHandler(final StorageHandler<?> storageHandler, final Type getter, final String key, final Method m, final boolean primitive) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        this.getterHandler = this;

        this.type = getter;
        this.key = key;
        this.method = m;

        this.primitive = primitive;
        if (this.isGetter()) {
            this.getterHandler = this;
            this.rawClass = m.getReturnType();
        } else {
            this.setterHandler = this;
            this.rawClass = m.getParameterTypes()[0];
        }

        // get parent crypt infos
        this.crypted = storageHandler.isCrypted();
        this.cryptKey = storageHandler.getKey();
        // read local cryptinfos
        final CryptedStorage an = m.getAnnotation(CryptedStorage.class);
        if (an != null) {
            this.crypted = true;
            if (an.key() != null) {
                this.cryptKey = an.key();
                if (this.cryptKey.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + m + " is invalid"); }
            }
        }
        final PlainStorage anplain = m.getAnnotation(PlainStorage.class);
        if (anplain != null && this.crypted) {
            if (an != null) { throw new InterfaceParseException("Cannot Set CryptStorage and PlainStorage Annotation"); }
            // parent crypted, but plain for this single entry
            this.crypted = false;

        }
        /*
         * the following is all default value creation. To speed this up, we
         * could implement a @NoDefaultValues Annotation
         */
        // init defaultvalues. read from annotations
        if (primitive) {
            if (this.rawClass == Boolean.class || this.rawClass == boolean.class) {
                final DefaultBooleanValue ann = m.getAnnotation(DefaultBooleanValue.class);
                if (ann != null) {
                    this.defaultBoolean = ann.value();
                }
                this.checkBadAnnotations(m, DefaultBooleanValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == Long.class || this.rawClass == long.class) {
                final DefaultLongValue ann = m.getAnnotation(DefaultLongValue.class);
                if (ann != null) {
                    this.defaultLong = ann.value();
                }
                this.checkBadAnnotations(m, DefaultLongValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == Integer.class || this.rawClass == int.class) {
                final DefaultIntValue ann = m.getAnnotation(DefaultIntValue.class);
                if (ann != null) {
                    this.defaultInteger = ann.value();
                }
                this.checkBadAnnotations(m, DefaultIntValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == Byte.class || this.rawClass == byte.class) {
                final DefaultByteValue ann = m.getAnnotation(DefaultByteValue.class);
                if (ann != null) {
                    this.defaultByte = ann.value();
                }
                this.checkBadAnnotations(m, DefaultByteValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == Float.class || this.rawClass == float.class) {
                final DefaultFloatValue ann = m.getAnnotation(DefaultFloatValue.class);
                if (ann != null) {
                    this.defaultFloat = ann.value();
                }
                this.checkBadAnnotations(m, DefaultFloatValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == String.class) {
                final DefaultStringValue ann = m.getAnnotation(DefaultStringValue.class);
                if (ann != null) {
                    this.defaultString = ann.value();
                }
                this.checkBadAnnotations(m, DefaultStringValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == String[].class) {
                final DefaultStringArrayValue ann = m.getAnnotation(DefaultStringArrayValue.class);
                if (ann != null) {
                    this.defaultStringArray = ann.value();
                }
                this.checkBadAnnotations(m, DefaultStringArrayValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass.isEnum()) {
                final DefaultEnumValue ann = m.getAnnotation(DefaultEnumValue.class);
                if (ann != null) {
                    // chek if this is really the best way to convert string to
                    // enum
                    final int index = ann.value().lastIndexOf(".");
                    final String name = ann.value().substring(index + 1);
                    final String clazz = ann.value().substring(0, index);

                    this.defaultEnum = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);

                }
                this.checkBadAnnotations(m, DefaultEnumValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == Double.class || this.rawClass == double.class) {
                final DefaultDoubleValue ann = m.getAnnotation(DefaultDoubleValue.class);
                if (ann != null) {
                    this.defaultDouble = ann.value();
                }
                this.checkBadAnnotations(m, DefaultDoubleValue.class, CryptedStorage.class, PlainStorage.class);
            } else {
                throw new StorageException("Invalid datatype: " + this.rawClass);
            }
        } else {
            if (this.rawClass.isArray()) {
                final Class<?> ct = this.rawClass.getComponentType();
                if (ct == Boolean.class || ct == boolean.class) {
                    final DefaultBooleanArrayValue ann = m.getAnnotation(DefaultBooleanArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultBooleanArrayValue.class, CryptedStorage.class, PlainStorage.class);
                    this.checkBadAnnotations(m, DefaultBooleanArrayValue.class);
                } else if (ct == Long.class || ct == long.class) {

                    final DefaultLongArrayValue ann = m.getAnnotation(DefaultLongArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultLongArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Integer.class || ct == int.class) {

                    final DefaultIntArrayValue ann = m.getAnnotation(DefaultIntArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultIntArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Byte.class || ct == byte.class) {
                    final DefaultByteArrayValue ann = m.getAnnotation(DefaultByteArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultByteArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Float.class || ct == float.class) {

                    final DefaultFloatArrayValue ann = m.getAnnotation(DefaultFloatArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultFloatArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == String.class) {
                    /* obsolet here cause String[] is primitive */
                    final DefaultStringArrayValue ann = m.getAnnotation(DefaultStringArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultStringArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct.isEnum()) {
                    final DefaultEnumArrayValue ann = m.getAnnotation(DefaultEnumArrayValue.class);
                    if (ann != null) {
                        // chek if this is really the best way to convert string
                        // to
                        // enum
                        final Enum[] ret = new Enum[ann.value().length];
                        for (int i = 0; i < ret.length; i++) {
                            final int index = ann.value()[i].lastIndexOf(".");
                            final String name = ann.value()[i].substring(index + 1);
                            final String clazz = ann.value()[i].substring(0, index);

                            ret[i] = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);
                        }
                        this.defaultObject = ret;

                    }
                    this.checkBadAnnotations(m, DefaultEnumArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Double.class || ct == double.class) {
                    final DefaultDoubleArrayValue ann = m.getAnnotation(DefaultDoubleArrayValue.class);
                    if (ann != null) {
                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultDoubleArrayValue.class, CryptedStorage.class, PlainStorage.class);
                }
            }

            if (this.defaultObject == null) {
                // if rawtypoe is an storable, we can have defaultvalues in
                // json7String from
                final DefaultObjectValue ann = m.getAnnotation(DefaultObjectValue.class);
                if (ann != null) {
                    this.defaultObject = JSonStorage.restoreFromString(ann.value(), this.rawClass);
                }
                // if not, we create an empty storable instance

                if (this.defaultObject == null) {
                    if (Storable.class.isAssignableFrom(this.rawClass)) {
                        Constructor<?> c;

                        c = this.rawClass.getDeclaredConstructor(new Class[] {});
                        c.setAccessible(true);
                        this.defaultObject = c.newInstance(null);

                    }
                }
                this.checkBadAnnotations(m, DefaultObjectValue.class, CryptedStorage.class, PlainStorage.class);
            }

        }
    }

    /**
     * @param m
     * @param class1
     */
    private void checkBadAnnotations(final Method m, final Class<? extends Annotation>... classes) {
        /**
         * This main mark is important!!
         */
        main: for (final Annotation a : m.getAnnotations()) {
            for (final Class<? extends Annotation> ok : classes) {
                if (ok.isAssignableFrom(a.getClass())) {
                    continue main;
                }
            }
            throw new InterfaceParseException("Bad Annotation: " + a + " for " + m);
        }

    }

    public boolean getDefaultBoolean() {
        return this.defaultBoolean;
    }

    public byte getDefaultByte() {
        return this.defaultByte;
    }

    public double getDefaultDouble() {
        return this.defaultDouble;
    }

    public Enum<?> getDefaultEnum() {
        return this.defaultEnum;
    }

    public float getDefaultFloat() {
        return this.defaultFloat;
    }

    public int getDefaultInteger() {
        return this.defaultInteger;
    }

    public long getDefaultLong() {
        return this.defaultLong;
    }

    public Object getDefaultObject() {
        return this.defaultObject;
    }

    public String getDefaultString() {
        return this.defaultString;
    }

    public String[] getDefaultStringArray() {
        return this.defaultStringArray;
    }

    public String getKey() {
        return this.key;
    }

    public Method getMethod() {
        return this.method;
    }

    private String getObjectKey() {
        return "cfg/config_" + this.method.getDeclaringClass().getSimpleName() + "." + this.key + "." + (this.isCrypted() ? "ejs" : "json");
    }

    public Class<?> getRawClass() {
        return this.rawClass;
    }

    public Type getType() {
        return this.type;
    }

    /**
     * @return
     */
    private boolean isCrypted() {
        return this.crypted;
    }

    /**
     * @return
     */
    public boolean isGetter() {

        return this.type == Type.GETTER;
    }

    public boolean isPrimitive() {
        return this.primitive;
    }

    /**
     * @return
     */
    public Object read() {
        return JSonStorage.restoreFrom(Application.getResource(this.getObjectKey()), !this.crypted, this.cryptKey, new TypeRef(this.method.getGenericReturnType()), this.defaultObject);
    }

    public void setDefaultBoolean(final boolean defaultBoolean) {
        this.defaultBoolean = defaultBoolean;
    }

    public void setDefaultByte(final byte defaultByte) {
        this.defaultByte = defaultByte;
    }

    public void setDefaultDouble(final double defaultDouble) {
        this.defaultDouble = defaultDouble;
    }

    public void setDefaultEnum(final Enum<?> defaultEnum) {
        this.defaultEnum = defaultEnum;
    }

    public void setDefaultFloat(final float defaultFloat) {
        this.defaultFloat = defaultFloat;
    }

    public void setDefaultInteger(final int defaultInteger) {
        this.defaultInteger = defaultInteger;
    }

    public void setDefaultLong(final long defaultLong) {
        this.defaultLong = defaultLong;
    }

    public void setDefaultObject(final Object defaultObject) {
        this.defaultObject = defaultObject;
    }

    public void setDefaultString(final String defaultString) {
        this.defaultString = defaultString;
    }

    /**
     * @param h
     */
    public void setGetter(final MethodHandler h) {
        this.getterHandler = h;
        this.validDateCrypt();
    }

    public void setRawClass(final Class<?> rawClass) {
        this.rawClass = rawClass;

    }

    /**
     * @param setterhandler
     */
    public void setSetter(final MethodHandler setterhandler) {
        this.setterHandler = setterhandler;
        this.validDateCrypt();
    }

    public String toString() {
        return this.method + "";
    }

    /**
     * 
     */
    private void validDateCrypt() {
        if (this.getterHandler.isCrypted() != this.setterHandler.isCrypted()) { throw new InterfaceParseException(this.getterHandler + " cryptsettings != " + this.setterHandler); }// check
        // keys
        if (this.getterHandler.isCrypted()) {
            for (int i = 0; i < this.getterHandler.cryptKey.length; i++) {
                if (this.getterHandler.cryptKey[i] != this.setterHandler.cryptKey[i]) { throw new InterfaceParseException(this.getterHandler + " cryptkey mismatch" + this.setterHandler); }
            }

        }
    }

    /**
     * @param object
     */
    public void write(final Object object) {
        JSonStorage.saveTo(this.getObjectKey(), JSonStorage.serializeToJson(object), this.cryptKey);

    }
}
