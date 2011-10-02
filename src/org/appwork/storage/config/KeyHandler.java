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
import java.lang.reflect.Method;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.StorageException;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.AllowStorage;
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
import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.annotations.PlainStorage;
import org.appwork.storage.config.annotations.RangeValidator;
import org.appwork.storage.config.annotations.RegexValidator;
import org.appwork.storage.config.annotations.RequiresRestart;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.utils.logging.Log;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * 
 */
public class KeyHandler<RawClass> {

    private static final int             MIN_LIFETIME            = 10000;
    private static final String          ANNOTATION_PACKAGE_NAME = CryptedStorage.class.getPackage().getName();
    private static final String          PACKAGE_NAME            = PlainStorage.class.getPackage().getName();
    private final String                 key;
    private MethodHandler                getter;
    private MethodHandler                setter;
    private final StorageHandler<?>      storageHandler;
    private boolean                      primitive;
    private boolean                      defaultBoolean          = false;
    private long                         defaultLong             = 0l;
    private int                          defaultInteger          = 0;
    private Enum<?>                      defaultEnum             = null;
    private double                       defaultDouble           = 0.0d;
    private String                       defaultString           = null;
    private byte                         defaultByte             = 0;
    private Object                       defaultObject           = null;
    private float                        defaultFloat            = 0.0f;
    private boolean                      crypted;
    private byte[]                       cryptKey;
    private File                         path;
    private Object                       defaultFactory;
    private MinTimeWeakReference<Object> cache;

    /**
     * @param storageHandler
     * @param key2
     */
    protected KeyHandler(final StorageHandler<?> storageHandler, final String key) {
        this.storageHandler = storageHandler;
        this.key = key;
        // get parent crypt infos
        this.crypted = storageHandler.isCrypted();
        this.cryptKey = storageHandler.getKey();
        // this.refQueue = new ReferenceQueue<Object>();

    }

    private void checkBadAnnotations(final Class<? extends Annotation>... classes) {
        this.checkBadAnnotations(this.getter.getMethod(), classes);
        if (this.setter != null) {
            this.checkBadAnnotations(this.setter.getMethod(), classes);
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
        final Class<?>[] okForAll = new Class<?>[] { AboutConfig.class,RequiresRestart.class, AllowStorage.class, Description.class, CryptedStorage.class, PlainStorage.class };
        final Class<?>[] clazzes = new Class<?>[classes.length + okForAll.length];
        System.arraycopy(classes, 0, clazzes, 0, classes.length);
        System.arraycopy(okForAll, 0, clazzes, classes.length, okForAll.length);

        main: for (final Annotation a : m.getAnnotations()) {
            // all other Annotations are ok anyway

            final String aName = a.annotationType().getName();
            if (!aName.startsWith(KeyHandler.PACKAGE_NAME)) {
                continue;
            }

            for (final Class<?> ok : clazzes) {
                if (ok.isAssignableFrom(a.getClass())) {
                    continue main;
                }
            }
            throw new InterfaceParseException("Bad Annotation: " + a + " for " + m);
        }

    }

    /**
     * @param <T>
     * @param class1
     * @return
     */
    public <T extends Annotation> T getAnnotation(final Class<T> class1) {

        T ret = this.getter.getMethod().getAnnotation(class1);
        if (ret == null && this.setter != null) {
            ret = this.setter.getMethod().getAnnotation(class1);
        } else if (this.setter != null && this.setter.getMethod().getAnnotation(class1) != null) {

            if (KeyHandler.ANNOTATION_PACKAGE_NAME.equals(class1.getPackage().getName())) { throw new InterfaceParseException("Dupe Annotation in  " + this + " (" + class1 + ")"); }
        }
        return ret;
    }

    protected boolean getDefaultBoolean() {
        if (this.defaultFactory != null) {
            try {

                return (Boolean) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        ;
        return this.defaultBoolean;
    }

    protected byte getDefaultByte() {
        if (this.defaultFactory != null) {
            try {

                return (Byte) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultByte;
    }

    protected double getDefaultDouble() {
        if (this.defaultFactory != null) {
            try {

                return (Double) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultDouble;
    }

    protected Enum<?> getDefaultEnum() {
        if (this.defaultFactory != null) {
            try {

                final Enum<?> ret = (Enum<?>) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
                if (ret != null) { return ret; }
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultEnum;
    }

    protected float getDefaultFloat() {
        if (this.defaultFactory != null) {
            try {

                return (Float) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultFloat;
    }

    protected int getDefaultInteger() {
        if (this.defaultFactory != null) {
            try {

                return (Integer) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultInteger;
    }

    protected long getDefaultLong() {
        if (this.defaultFactory != null) {
            try {

                return (Long) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultLong;
    }

    protected Object getDefaultObject() {
        if (this.defaultFactory != null) {
            try {

                return this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return this.defaultObject;
    }

    protected String getDefaultString() {
        if (this.defaultFactory != null) {
            try {
                final String ret = (String) this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
                return ret;
            } catch (final Exception e) {
                Log.exception(e);
            }
        }

        return this.defaultString;
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public RawClass getDefaultValue() {
        if (this.isPrimitive()) {
            if (Clazz.isBoolean(this.getter.getRawClass())) {
                return (RawClass) new Boolean(this.getDefaultBoolean());
            } else if (Clazz.isLong(this.getter.getRawClass())) {
                return (RawClass) new Long(this.getDefaultLong());
            } else if (Clazz.isInteger(this.getter.getRawClass())) {
                return (RawClass) new Integer(this.getDefaultInteger());
            } else if (Clazz.isByte(this.getter.getRawClass())) {
                return (RawClass) new Byte(this.getDefaultByte());
            } else if (Clazz.isFloat(this.getter.getRawClass())) {
                return (RawClass) new Float(this.getDefaultFloat());
            } else if (this.getRawClass() == String.class) {
                return (RawClass) this.getDefaultString();
            } else if (this.getRawClass().isEnum()) {

                return (RawClass) this.getDefaultEnum();
            } else if (Clazz.isDouble(this.getRawClass())) {
                return (RawClass) new Double(this.getDefaultDouble());
            } else {
                return null;
            }
        } else {
            return (RawClass) this.getDefaultObject();

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
    @SuppressWarnings("unchecked")
    public Class<RawClass> getRawClass() {
        return (Class<RawClass>) this.getter.getRawClass();
    }

    public MethodHandler getSetter() {
        return this.setter;
    }

    protected StorageHandler<?> getStorageHandler() {
        return this.storageHandler;
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected RawClass getValue() {

        if (this.isPrimitive()) {

            return (RawClass) this.storageHandler.getPrimitive(this);

        } else {
            Object ret = this.cache != null ? this.cache.get() : null;
            if (ret == null) {
                ret = this.read();

                this.cache = new MinTimeWeakReference<Object>(ret, KeyHandler.MIN_LIFETIME, "Storage " + this.getKey());

            }
            return (RawClass) ret;
        }
    }

    /**
     * @throws ClassNotFoundException
     * 
     */
    @SuppressWarnings("unchecked")
    protected void init() throws ClassNotFoundException {
        // read local cryptinfos
        this.primitive = JSonStorage.canStorePrimitive(this.getter.getMethod().getReturnType());
        final CryptedStorage an = this.getAnnotation(CryptedStorage.class);
        if (an != null) {
            this.crypted = true;
            if (an.key() != null) {
                this.cryptKey = an.key();
                if (this.cryptKey.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + this + " is invalid"); }
            }
        }
        final PlainStorage anplain = this.getAnnotation(PlainStorage.class);
        if (anplain != null && this.crypted) {
            if (an != null) { throw new InterfaceParseException("Cannot Set CryptStorage and PlainStorage Annotation"); }
            // parent crypted, but plain for this single entry
            this.crypted = false;

        }

        this.path = new File(this.storageHandler.getPath() + "." + this.key + "." + (this.isCrypted() ? "ejs" : "json"));

        /*
         * the following is all default value creation. To speed this up, we
         * could implement a @NoDefaultValues Annotation
         */
        // init defaultvalues. read from annotations
        this.defaultFactory = this.storageHandler.getDefaultFactory();
        final boolean hasDefaultFactory = this.storageHandler.getDefaultFactory() != null;
        
        // ,RegexValidator.class,
        if (this.primitive) {
            if (Clazz.isBoolean(this.getter.getRawClass())) {
                final DefaultBooleanValue ann = this.getAnnotation(DefaultBooleanValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }
                    this.defaultBoolean = ann.value();
                }
                this.checkBadAnnotations(DefaultBooleanValue.class);
            } else if (Clazz.isLong(this.getter.getRawClass())) {
                final DefaultLongValue ann = this.getAnnotation(DefaultLongValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultLong = ann.value();
                }
                this.checkBadAnnotations(DefaultLongValue.class, SpinnerValidator.class, RangeValidator.class);
            } else if (Clazz.isInteger(this.getter.getRawClass())) {
                final DefaultIntValue ann = this.getAnnotation(DefaultIntValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultInteger = ann.value();
                }
                this.checkBadAnnotations(DefaultIntValue.class,SpinnerValidator.class, RangeValidator.class);
            } else if (Clazz.isByte(this.getter.getRawClass())) {
                final DefaultByteValue ann = this.getAnnotation(DefaultByteValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultByte = ann.value();
                }
                this.checkBadAnnotations(DefaultByteValue.class,SpinnerValidator.class, RangeValidator.class);
            } else if (Clazz.isFloat(this.getter.getRawClass())) {
                final DefaultFloatValue ann = this.getAnnotation(DefaultFloatValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultFloat = ann.value();
                }
                this.checkBadAnnotations(DefaultFloatValue.class);
            } else if (this.getter.getRawClass() == String.class) {
                final DefaultStringValue ann = this.getAnnotation(DefaultStringValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultString = ann.value();
                }

                this.checkBadAnnotations(DefaultStringValue.class,RegexValidator.class);
            } else if (this.getter.getRawClass().isEnum()) {

                final DefaultEnumValue ann = this.getAnnotation(DefaultEnumValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    // chek if this is really the best way to convert string to
                    // enum
                    final int index = ann.value().lastIndexOf(".");
                    final String name = ann.value().substring(index + 1);
                    final String clazz = ann.value().substring(0, index);

                    this.defaultEnum = Enum.valueOf((Class<Enum>) Class.forName(clazz), name);

                }
                this.checkBadAnnotations(DefaultEnumValue.class);
            } else if (Clazz.isDouble(this.getter.getRawClass())) {
                final DefaultDoubleValue ann = this.getAnnotation(DefaultDoubleValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                    this.defaultDouble = ann.value();
                }
                this.checkBadAnnotations(DefaultDoubleValue.class);
            } else {
                throw new StorageException("Invalid datatype: " + this.getter.getRawClass());
            }
        } else {
            if (this.getRawClass().isArray()) {
                final Class<?> ct = this.getter.getRawClass().getComponentType();
                if (Clazz.isBoolean(ct)) {
                    final DefaultBooleanArrayValue ann = this.getAnnotation(DefaultBooleanArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultBooleanArrayValue.class);
                    this.checkBadAnnotations(DefaultBooleanArrayValue.class);
                } else if (Clazz.isLong(ct)) {

                    final DefaultLongArrayValue ann = this.getAnnotation(DefaultLongArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultLongArrayValue.class,SpinnerValidator.class, RangeValidator.class);
                } else if (Clazz.isInteger(ct)) {

                    final DefaultIntArrayValue ann = this.getAnnotation(DefaultIntArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultIntArrayValue.class,SpinnerValidator.class, RangeValidator.class);
                } else if (Clazz.isByte(ct)) {
                    final DefaultByteArrayValue ann = this.getAnnotation(DefaultByteArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultByteArrayValue.class,SpinnerValidator.class, RangeValidator.class);
                } else if (Clazz.isFloat(ct)) {

                    final DefaultFloatArrayValue ann = this.getAnnotation(DefaultFloatArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultFloatArrayValue.class);
                } else if (ct == String.class) {
                    /* obsolet here cause String[] is primitive */
                    final DefaultStringArrayValue ann = this.getAnnotation(DefaultStringArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultStringArrayValue.class,RegexValidator.class);
                } else if (ct.isEnum()) {
                    final DefaultEnumArrayValue ann = this.getAnnotation(DefaultEnumArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

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
                    this.checkBadAnnotations(DefaultEnumArrayValue.class);
                } else if (Clazz.isDouble(ct)) {
                    final DefaultDoubleArrayValue ann = this.getAnnotation(DefaultDoubleArrayValue.class);
                    if (ann != null) {
                        if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }

                        this.defaultObject = ann.value();
                    }
                    this.checkBadAnnotations(DefaultDoubleArrayValue.class);
                }
            }

            if (this.defaultObject == null) {
                // if rawtypoe is an storable, we can have defaultvalues in
                // json7String from
                final DefaultObjectValue ann = this.getAnnotation(DefaultObjectValue.class);
                if (ann != null) {
                    if (hasDefaultFactory) { throw new InterfaceParseException(this + " is not allowed to have defaultvalues because interface has a Defaultfactory. Set Defaults in " + this.storageHandler.getDefaultFactory().getClass().getName() + "'s getters"); }
                    final String v = ann.value();
                    this.defaultObject = JSonStorage.restoreFromString(v, new TypeRef<Object>(this.getter.getMethod().getGenericReturnType()) {
                    }, null);
                }

                this.checkBadAnnotations(DefaultObjectValue.class);
            }
            

        }
    }

    /**
     * @return
     */
    protected boolean isCrypted() {
        return this.crypted;
    }

    /**
     * @param m
     * @return
     */
    protected boolean isGetter(final Method m) {

        return m.equals(this.getter.getMethod());
    }

    protected boolean isPrimitive() {
        return this.primitive;
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected RawClass read() {
        try {
            Log.L.finer("Read Config: " + this.path.getAbsolutePath());
            if (this.defaultFactory != null) {
                final Object dummy = new Object();
                Object ret = JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, new TypeRef(this.getter.getMethod().getGenericReturnType()) {
                }, dummy);
                if (ret == dummy) {
                    try {
                        ret = this.getter.getMethod().invoke(this.defaultFactory, new Object[] {});
                    } catch (final Exception e) {
                        Log.exception(e);
                        ret = null;
                    }
                    this.defaultObject = ret;
                }
                return (RawClass) ret;
            } else {
                return (RawClass) JSonStorage.restoreFrom(this.path, !this.crypted, this.cryptKey, new TypeRef(this.getter.getMethod().getGenericReturnType()) {
                }, this.defaultObject);
            }

        } finally {
            if (!this.path.exists()) {
                this.write((RawClass) this.defaultObject);
            }

        }
    }

    /**
     * @param h
     */
    protected void setGetter(final MethodHandler h) {
        this.getter = h;

    }

    /**
     * @param h
     */
    protected void setSetter(final MethodHandler h) {
        this.setter = h;

    }

    /**
     * @param object
     */
    protected void setValue(final RawClass object) throws Throwable {
        final Object validator = this.storageHandler.getValidator();
        if (validator != null) {
            // we call the defaultfactory setter to validate

            this.getSetter().getMethod().invoke(validator, new Object[] { object });

        }
        if (this.isPrimitive()) {

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
            this.write(object);
            this.cache = new MinTimeWeakReference<Object>(object, KeyHandler.MIN_LIFETIME, "Storage " + this.getKey());

        }
    }

    @Override
    public String toString() {
        return "Keyhandler " + this.storageHandler.getConfigInterface() + "." + this.getKey();
    }

    /**
     * @param object
     */
    protected void write(final RawClass object) {

        JSonStorage.saveTo(this.path, !this.crypted, this.cryptKey, JSonStorage.serializeToJson(object));

    }

    /**
     * @return
     */
    public Class<?> getDeclaringClass() {
        if (getter != null) {
            return getter.getMethod().getDeclaringClass();
        } else {
            return setter.getMethod().getDeclaringClass();
        }

    }

}
