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

import java.io.File;
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
import org.appwork.utils.logging.Log;

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
    private boolean       defaultBoolean = false;
    private long          defaultLong    = 0l;
    private int           defaultInteger = 0;
    private Enum<?>       defaultEnum    = null;
    private double        defaultDouble  = 0.0d;
    private String        defaultString  = null;
    private byte          defaultByte    = 0;
    private Object        defaultObject  = null;
    private float         defaultFloat   = 0.0f;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MethodHandler(final StorageHandler<?> storageHandler, final Type getter, final String key, final Method m, final boolean primitive) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        getterHandler = this;

        type = getter;
        this.key = key;
        method = m;

        this.primitive = primitive;
        if (isGetter()) {
            getterHandler = this;
            rawClass = m.getReturnType();
        } else {
            setterHandler = this;
            rawClass = m.getParameterTypes()[0];
        }

        // get parent crypt infos
        crypted = storageHandler.isCrypted();
        cryptKey = storageHandler.getKey();
        // read local cryptinfos
        final CryptedStorage an = m.getAnnotation(CryptedStorage.class);
        if (an != null) {
            crypted = true;
            if (an.key() != null) {
                cryptKey = an.key();
                if (cryptKey.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + m + " is invalid"); }
            }
        }
        final PlainStorage anplain = m.getAnnotation(PlainStorage.class);
        if (anplain != null && crypted) {
            if (an != null) { throw new InterfaceParseException("Cannot Set CryptStorage and PlainStorage Annotation"); }
            // parent crypted, but plain for this single entry
            crypted = false;

        }
        /*
         * the following is all default value creation. To speed this up, we
         * could implement a @NoDefaultValues Annotation
         */
        // init defaultvalues. read from annotations
        if (primitive) {
            if (rawClass == Boolean.class || rawClass == boolean.class) {
                final DefaultBooleanValue ann = m.getAnnotation(DefaultBooleanValue.class);
                if (ann != null) {
                    defaultBoolean = ann.value();
                }
                checkBadAnnotations(m, DefaultBooleanValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == Long.class || rawClass == long.class) {
                final DefaultLongValue ann = m.getAnnotation(DefaultLongValue.class);
                if (ann != null) {
                    defaultLong = ann.value();
                }
                checkBadAnnotations(m, DefaultLongValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == Integer.class || rawClass == int.class) {
                final DefaultIntValue ann = m.getAnnotation(DefaultIntValue.class);
                if (ann != null) {
                    defaultInteger = ann.value();
                }
                checkBadAnnotations(m, DefaultIntValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == Byte.class || rawClass == byte.class) {
                final DefaultByteValue ann = m.getAnnotation(DefaultByteValue.class);
                if (ann != null) {
                    defaultByte = ann.value();
                }
                checkBadAnnotations(m, DefaultByteValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == Float.class || rawClass == float.class) {
                final DefaultFloatValue ann = m.getAnnotation(DefaultFloatValue.class);
                if (ann != null) {
                    defaultFloat = ann.value();
                }
                checkBadAnnotations(m, DefaultFloatValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == String.class) {
                final DefaultStringValue ann = m.getAnnotation(DefaultStringValue.class);
                if (ann != null) {
                    defaultString = ann.value();
                }

                checkBadAnnotations(m, DefaultStringValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass.isEnum()) {

                final DefaultEnumValue ann = m.getAnnotation(DefaultEnumValue.class);
                if (ann != null) {
                    // chek if this is really the best way to convert string to
                    // enum
                    final int index = ann.value().lastIndexOf(".");
                    final String name = ann.value().substring(index + 1);
                    final String clazz = ann.value().substring(0, index);

                    defaultEnum = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);

                }
                checkBadAnnotations(m, DefaultEnumValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (rawClass == Double.class || rawClass == double.class) {
                final DefaultDoubleValue ann = m.getAnnotation(DefaultDoubleValue.class);
                if (ann != null) {
                    defaultDouble = ann.value();
                }
                checkBadAnnotations(m, DefaultDoubleValue.class, CryptedStorage.class, PlainStorage.class);
            } else {
                throw new StorageException("Invalid datatype: " + rawClass);
            }
        } else {
            if (rawClass.isArray()) {
                final Class<?> ct = rawClass.getComponentType();
                if (ct == Boolean.class || ct == boolean.class) {
                    final DefaultBooleanArrayValue ann = m.getAnnotation(DefaultBooleanArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultBooleanArrayValue.class, CryptedStorage.class, PlainStorage.class);
                    checkBadAnnotations(m, DefaultBooleanArrayValue.class);
                } else if (ct == Long.class || ct == long.class) {

                    final DefaultLongArrayValue ann = m.getAnnotation(DefaultLongArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultLongArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Integer.class || ct == int.class) {

                    final DefaultIntArrayValue ann = m.getAnnotation(DefaultIntArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultIntArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Byte.class || ct == byte.class) {
                    final DefaultByteArrayValue ann = m.getAnnotation(DefaultByteArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultByteArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Float.class || ct == float.class) {

                    final DefaultFloatArrayValue ann = m.getAnnotation(DefaultFloatArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultFloatArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == String.class) {
                    /* obsolet here cause String[] is primitive */
                    final DefaultStringArrayValue ann = m.getAnnotation(DefaultStringArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultStringArrayValue.class, CryptedStorage.class, PlainStorage.class);
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
                        defaultObject = ret;

                    }
                    checkBadAnnotations(m, DefaultEnumArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == Double.class || ct == double.class) {
                    final DefaultDoubleArrayValue ann = m.getAnnotation(DefaultDoubleArrayValue.class);
                    if (ann != null) {
                        defaultObject = ann.value();
                    }
                    checkBadAnnotations(m, DefaultDoubleArrayValue.class, CryptedStorage.class, PlainStorage.class);
                }
            }

            if (defaultObject == null) {
                // if rawtypoe is an storable, we can have defaultvalues in
                // json7String from
                final DefaultObjectValue ann = m.getAnnotation(DefaultObjectValue.class);
                if (ann != null) {
                    defaultObject = JSonStorage.restoreFromString(ann.value(), rawClass);
                }
                // if not, we create an empty storable instance

                if (defaultObject == null) {
                    if (Storable.class.isAssignableFrom(rawClass)) {
                        Constructor<?> c = rawClass.getDeclaredConstructor(new Class[] {});
                        c.setAccessible(true);
                        defaultObject = c.newInstance((Object[]) null);
                    }
                }
                checkBadAnnotations(m, DefaultObjectValue.class, CryptedStorage.class, PlainStorage.class);
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
        return defaultBoolean;
    }

    public byte getDefaultByte() {
        return defaultByte;
    }

    public double getDefaultDouble() {
        return defaultDouble;
    }

    public Enum<?> getDefaultEnum() {
        return defaultEnum;
    }

    public float getDefaultFloat() {
        return defaultFloat;
    }

    public int getDefaultInteger() {
        return defaultInteger;
    }

    public long getDefaultLong() {
        return defaultLong;
    }

    public Object getDefaultObject() {
        return defaultObject;
    }

    public String getDefaultString() {
        return defaultString;
    }

    public String getKey() {
        return key;
    }

    public Method getMethod() {
        return method;
    }

    private String getObjectKey() {
        return "cfg/config_" + method.getDeclaringClass().getSimpleName() + "." + key + "." + (isCrypted() ? "ejs" : "json");
    }

    public Class<?> getRawClass() {
        return rawClass;
    }

    public Type getType() {
        return type;
    }

    /**
     * @return
     */
    private boolean isCrypted() {
        return crypted;
    }

    /**
     * @return
     */
    public boolean isGetter() {

        return type == Type.GETTER;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * @return
     */
    public Object read() {
        final File file = Application.getResource(getObjectKey());
        try {
            Log.L.finer("Read Config: " + file.getAbsolutePath());
            return JSonStorage.restoreFrom(file, !crypted, cryptKey, new TypeRef(method.getGenericReturnType()), defaultObject);
        } finally {
            if (!file.exists()) {
                write(defaultObject);
            }

        }
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
        getterHandler = h;
        validDateCrypt();
    }

    public void setRawClass(final Class<?> rawClass) {
        this.rawClass = rawClass;

    }

    /**
     * @param setterhandler
     */
    public void setSetter(final MethodHandler setterhandler) {
        setterHandler = setterhandler;
        validDateCrypt();
    }

    @Override
    public String toString() {
        return method + "";
    }

    /**
     * 
     */
    private void validDateCrypt() {
        if (getterHandler.isCrypted() != setterHandler.isCrypted()) { throw new InterfaceParseException(getterHandler + " cryptsettings != " + setterHandler); }// check
        // keys

        if (getterHandler.isCrypted()) {
            for (int i = 0; i < getterHandler.cryptKey.length; i++) {
                if (getterHandler.cryptKey[i] != setterHandler.cryptKey[i]) { throw new InterfaceParseException(getterHandler + " cryptkey mismatch" + setterHandler); }

            }

        }
    }

    /**
     * @param object
     */
    public void write(final Object object) {
        JSonStorage.saveTo(getObjectKey(), JSonStorage.serializeToJson(object), cryptKey);

    }
}
