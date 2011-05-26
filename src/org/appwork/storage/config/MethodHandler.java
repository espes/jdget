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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.StorageException;
import org.appwork.storage.TypeRef;
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
import org.appwork.utils.logging.Log;
import org.appwork.utils.reflection.Clazz;

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

    private boolean       crypted;
    private byte[]        cryptKey;
    private final File    path;
    private final Object  defaultFactory;

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

        this.type = getter;
        this.key = key;
        this.method = m;

        this.primitive = primitive;
        if (this.isGetter()) {

            this.rawClass = m.getReturnType();
        } else {

            this.rawClass = m.getParameterTypes()[0];
        }

        // get parent crypt infos
        this.crypted = storageHandler.isCrypted();
        this.cryptKey = storageHandler.getKey();
        this.path = new File(storageHandler.getPath() + "." + this.key + "." + (this.isCrypted() ? "ejs" : "json"));
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
        this.defaultFactory = storageHandler.getDefaultFactory();
        final boolean hasDefaultFactory = storageHandler.getDefaultFactory() != null;
        if (primitive) {
            if (Clazz.isBoolean(this.rawClass)) {
                final DefaultBooleanValue ann = m.getAnnotation(DefaultBooleanValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }
                    this.defaultBoolean = ann.value();
                }
                this.checkBadAnnotations(m, DefaultBooleanValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (Clazz.isLong(this.rawClass)) {
                final DefaultLongValue ann = m.getAnnotation(DefaultLongValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultLong = ann.value();
                }
                this.checkBadAnnotations(m, DefaultLongValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (Clazz.isInteger(this.rawClass)) {
                final DefaultIntValue ann = m.getAnnotation(DefaultIntValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultInteger = ann.value();
                }
                this.checkBadAnnotations(m, DefaultIntValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (Clazz.isByte(this.rawClass)) {
                final DefaultByteValue ann = m.getAnnotation(DefaultByteValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultByte = ann.value();
                }
                this.checkBadAnnotations(m, DefaultByteValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (Clazz.isFloat(this.rawClass)) {
                final DefaultFloatValue ann = m.getAnnotation(DefaultFloatValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultFloat = ann.value();
                }
                this.checkBadAnnotations(m, DefaultFloatValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass == String.class) {
                final DefaultStringValue ann = m.getAnnotation(DefaultStringValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultString = ann.value();
                }

                this.checkBadAnnotations(m, DefaultStringValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (this.rawClass.isEnum()) {

                final DefaultEnumValue ann = m.getAnnotation(DefaultEnumValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    // chek if this is really the best way to convert string to
                    // enum
                    final int index = ann.value().lastIndexOf(".");
                    final String name = ann.value().substring(index + 1);
                    final String clazz = ann.value().substring(0, index);

                    this.defaultEnum = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);

                }
                this.checkBadAnnotations(m, DefaultEnumValue.class, CryptedStorage.class, PlainStorage.class);
            } else if (Clazz.isDouble(this.rawClass)) {
                final DefaultDoubleValue ann = m.getAnnotation(DefaultDoubleValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultDouble = ann.value();
                }
                this.checkBadAnnotations(m, DefaultDoubleValue.class, CryptedStorage.class, PlainStorage.class);
            } else {
                throw new StorageException("Invalid datatype: " + this.rawClass);
            }
        } else {
            if (this.rawClass.isArray()) {
                final Class<?> ct = this.rawClass.getComponentType();
                if (Clazz.isBoolean(ct)) {
                    final DefaultBooleanArrayValue ann = m.getAnnotation(DefaultBooleanArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultBooleanArrayValue.class, CryptedStorage.class, PlainStorage.class);
                    this.checkBadAnnotations(m, DefaultBooleanArrayValue.class);
                } else if (Clazz.isLong(ct)) {

                    final DefaultLongArrayValue ann = m.getAnnotation(DefaultLongArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultLongArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (Clazz.isInteger(ct)) {

                    final DefaultIntArrayValue ann = m.getAnnotation(DefaultIntArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultIntArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (Clazz.isByte(ct)) {
                    final DefaultByteArrayValue ann = m.getAnnotation(DefaultByteArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultByteArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (Clazz.isFloat(ct)) {

                    final DefaultFloatArrayValue ann = m.getAnnotation(DefaultFloatArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultFloatArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct == String.class) {
                    /* obsolet here cause String[] is primitive */
                    final DefaultStringArrayValue ann = m.getAnnotation(DefaultStringArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(m, DefaultStringArrayValue.class, CryptedStorage.class, PlainStorage.class);
                } else if (ct.isEnum()) {
                    final DefaultEnumArrayValue ann = m.getAnnotation(DefaultEnumArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

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
                } else if (Clazz.isDouble(ct)) {
                    final DefaultDoubleArrayValue ann = m.getAnnotation(DefaultDoubleArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

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
                    if (hasDefaultFactory) { throw new InterfaceParseException(m + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultObject = JSonStorage.restoreFromString(ann.value(), this.rawClass);
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
        final String packag = PlainStorage.class.getPackage().getName();
        main: for (final Annotation a : m.getAnnotations()) {
            // all other Annotations are ok anyway
            if (!a.getClass().getName().startsWith(packag)) {
                continue;
            }

            for (final Class<? extends Annotation> ok : classes) {
                if (ok.isAssignableFrom(a.getClass())) {
                    continue main;
                }
            }
            throw new InterfaceParseException("Bad Annotation: " + a + " for " + m);
        }

    }

    public byte[] getCryptKey() {
        return this.cryptKey;
    }

    public boolean getDefaultBoolean() {
        if (this.defaultFactory != null) {
            try {

                return (Boolean) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        ;
        return this.defaultBoolean;
    }

    public byte getDefaultByte() {
        if (this.defaultFactory != null) {
            try {

                return (Byte) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultByte;
    }

    public double getDefaultDouble() {
        if (this.defaultFactory != null) {
            try {

                return (Double) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultDouble;
    }

    public Enum<?> getDefaultEnum() {
        if (this.defaultFactory != null) {
            try {

                final Enum<?> ret = (Enum<?>) this.method.invoke(this.defaultFactory, new Object[] {});
                if (ret != null) { return ret; }
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultEnum;
    }

    public float getDefaultFloat() {
        if (this.defaultFactory != null) {
            try {

                return (Float) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultFloat;
    }

    public int getDefaultInteger() {
        if (this.defaultFactory != null) {
            try {

                return (Integer) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultInteger;
    }

    public long getDefaultLong() {
        if (this.defaultFactory != null) {
            try {

                return (Long) this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultLong;
    }

    public Object getDefaultObject() {
        if (this.defaultFactory != null) {
            try {

                return this.method.invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultObject;
    }

    public String getDefaultString() {
        if (this.defaultFactory != null) {
            try {
                final String ret = (String) this.method.invoke(this.defaultFactory, new Object[] {});
                return ret;
            } catch (final Exception e) {
                Log.exception(e);
            }
        }

        return this.defaultString;
    }

    public String getKey() {
        return this.key;
    }

    public Method getMethod() {
        return this.method;
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
    public boolean isCrypted() {
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
    @SuppressWarnings("unchecked")
    public Object read() {
        try {
            Log.L.finer("Read Config: " + this.path.getAbsolutePath());
            if (this.defaultFactory != null) {
                final Object dummy = new Object();
                Object ret = JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, new TypeRef(this.method.getGenericReturnType()) {
                }, dummy);
                if (ret == dummy) {
                    try {
                        ret = this.method.invoke(this.defaultFactory, new Object[] {});
                    } catch (final Exception e) {
                        Log.exception(e);
                        ret = null;
                    }
                    this.defaultObject = ret;
                }
                return ret;
            } else {
                return JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, new TypeRef(this.method.getGenericReturnType()) {
                }, this.defaultObject);
            }

        } finally {
            if (!this.path.exists()) {
                this.write(this.defaultObject);
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

    public void setRawClass(final Class<?> rawClass) {
        this.rawClass = rawClass;

    }

    @Override
    public String toString() {
        return this.method + "";
    }

    /**
     * @param object
     */
    public void write(final Object object) {

        JSonStorage.saveTo(this.path, !this.crypted, this.cryptKey, JSonStorage.serializeToJson(object));

    }

}
