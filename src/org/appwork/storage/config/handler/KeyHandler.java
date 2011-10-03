/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.JsonKeyValueStorage;
import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.MethodHandler;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.AllowStorage;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultValue;
import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.annotations.PlainStorage;
import org.appwork.storage.config.annotations.RequiresRestart;

/**
 * @author thomas
 * 
 */
public abstract class KeyHandler<RawClass> {

    private static final String       ANNOTATION_PACKAGE_NAME = CryptedStorage.class.getPackage().getName();
    private static final String       PACKAGE_NAME            = PlainStorage.class.getPackage().getName();
    private final String              key;
    private MethodHandler             getter;
    protected MethodHandler           setter;
    protected final StorageHandler<?> storageHandler;
    private boolean                   primitive;
    protected RawClass                defaultValue;

    public RawClass getDefaultValue() {
        return defaultValue;
    }

    protected boolean             crypted;
    protected byte[]              cryptKey;
    protected JsonKeyValueStorage primitiveStorage;

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
        primitiveStorage = storageHandler.primitiveStorage;
        // this.refQueue = new ReferenceQueue<Object>();

    }

    protected void checkBadAnnotations(final Class<? extends Annotation>... class1) {
        this.checkBadAnnotations(this.getter.getMethod(), class1);
        if (this.setter != null) {
            this.checkBadAnnotations(this.setter.getMethod(), class1);
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
        final Class<?>[] okForAll = new Class<?>[] { DefaultValue.class, AboutConfig.class, RequiresRestart.class, AllowStorage.class, Description.class, CryptedStorage.class, PlainStorage.class };
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
    protected abstract RawClass getValue();

    /**
     * @throws Throwable
     * 
     */
    @SuppressWarnings("unchecked")
    protected void init() throws Throwable {
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
        checkBadAnnotations(getAllowedAnnotations());
        initHandler();

    }

    /**
     * @return
     */
    protected abstract Class<? extends Annotation>[] getAllowedAnnotations();

    /**
     * @throws Throwable
     * 
     */
    protected abstract void initHandler() throws Throwable;

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

        validateValue(object);
        putValue(object);

    }

    /**
     * @param object
     */
    protected abstract void validateValue(RawClass object) throws Throwable;

    /**
     * @param object
     */
    protected abstract void putValue(RawClass object);

    @Override
    public String toString() {
        return "Keyhandler " + this.storageHandler.getConfigInterface() + "." + this.getKey();
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
