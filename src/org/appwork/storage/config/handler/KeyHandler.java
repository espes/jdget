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
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.MethodHandler;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.AbstractValidator;
import org.appwork.storage.config.annotations.AllowStorage;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultFactory;
import org.appwork.storage.config.annotations.DefaultJsonObject;
import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.annotations.PlainStorage;
import org.appwork.storage.config.annotations.RequiresRestart;
import org.appwork.storage.config.annotations.ValidatorFactory;
import org.appwork.storage.config.defaults.AbstractDefaultFactory;
import org.appwork.storage.config.events.ConfigEvent;
import org.appwork.storage.config.events.ConfigEvent.Types;
import org.appwork.storage.config.events.ConfigEventSender;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * 
 */
public abstract class KeyHandler<RawClass> {

    private static final String                          ANNOTATION_PACKAGE_NAME = CryptedStorage.class.getPackage().getName();
    private static final String                          PACKAGE_NAME            = PlainStorage.class.getPackage().getName();
    private final String                                 key;
    private MethodHandler                                getter;
    protected String                                     defaultJson;
    protected Class<? extends AbstractDefaultFactory<?>> defaultFactoryClass;
    protected MethodHandler                              setter;
    protected final StorageHandler<?>                    storageHandler;
    private boolean                                      primitive;
    protected RawClass                                   defaultValue;

    protected boolean                                    crypted;

    protected byte[]                                     cryptKey;
    protected JsonKeyValueStorage                        primitiveStorage;
    private ConfigEventSender<RawClass>                  eventSender;
    private AbstractValidator<RawClass>                  validatorFactory;

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
        this.primitiveStorage = storageHandler.primitiveStorage;
        // this.refQueue = new ReferenceQueue<Object>();

    }

    protected void checkBadAnnotations(final Class<? extends Annotation>... class1) {
        int checker = 0;
        if (this.getAnnotation(this.getDefaultAnnotation()) != null) {
            checker++;
        }
        if (this.getAnnotation(DefaultJsonObject.class) != null) {
            checker++;
        }
        if (this.getAnnotation(DefaultFactory.class) != null) {
            checker++;
        }
        if (checker > 1) { throw new InterfaceParseException("Make sure that you use only one  of getDefaultAnnotation,DefaultObjectValue or DefaultValue "); }

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
        final Class<?>[] okForAll = new Class<?>[] { ValidatorFactory.class, DefaultJsonObject.class, DefaultFactory.class, AboutConfig.class, RequiresRestart.class, AllowStorage.class, Description.class, CryptedStorage.class, PlainStorage.class };
        final Class<?>[] clazzes = new Class<?>[classes.length + okForAll.length];
        System.arraycopy(classes, 0, clazzes, 0, classes.length);
        System.arraycopy(okForAll, 0, clazzes, classes.length, okForAll.length);

        main: for (final Annotation a : m.getAnnotations()) {
            // all other Annotations are ok anyway
            if (a == null) {
                continue;
            }
            final String aName = a.annotationType().getName();
            if (!aName.startsWith(KeyHandler.PACKAGE_NAME)) {
                continue;
            }
            if (this.getDefaultAnnotation() != null && this.getDefaultAnnotation().isAssignableFrom(a.getClass())) {
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
     * @param valueUpdated
     * @param keyHandler
     * @param object
     */
    private void fireEvent(final Types type, final KeyHandler<?> keyHandler, final Object parameter) {
        this.storageHandler.fireEvent(type, keyHandler, parameter);
        if (this.eventSender != null) {
            this.eventSender.fireEvent(new ConfigEvent(type, this, parameter));
        }

    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Annotation>[] getAllowedAnnotations() {

        return (Class<? extends Annotation>[]) new Class<?>[] {};

    }

    /**
     * @param <T>
     * @param class1
     * @return
     */
    public <T extends Annotation> T getAnnotation(final Class<T> class1) {
        if (class1 == null) { return null; }
        T ret = this.getter.getMethod().getAnnotation(class1);
        if (ret == null && this.setter != null) {
            ret = this.setter.getMethod().getAnnotation(class1);
        } else if (this.setter != null && this.setter.getMethod().getAnnotation(class1) != null) {

            if (KeyHandler.ANNOTATION_PACKAGE_NAME.equals(class1.getPackage().getName())) { throw new InterfaceParseException("Dupe Annotation in  " + this + " (" + class1 + ")"); }
        }
        return ret;
    }

    /**
     * @return
     */
    public Class<?> getDeclaringClass() {
        if (this.getter != null) {
            return this.getter.getMethod().getDeclaringClass();
        } else {
            return this.setter.getMethod().getDeclaringClass();
        }

    }

    protected Class<? extends Annotation> getDefaultAnnotation() {
        return null;
    }

    public RawClass getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Lazy initialiser of the eventsender. we do not wnat to create an
     * eventsender if nowbody uses it
     * 
     * @return
     */
    public synchronized ConfigEventSender<RawClass> getEventSender() {
        if (this.eventSender == null) {
            this.eventSender = new ConfigEventSender<RawClass>();
        }
        return this.eventSender;
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

    public StorageHandler<?> getStorageHandler() {
        return this.storageHandler;
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public abstract RawClass getValue();

    /**
     * @throws Throwable
     * 
     */
    @SuppressWarnings("unchecked")
    protected void init() throws Throwable {

        // read local cryptinfos
        this.primitive = JSonStorage.canStorePrimitive(this.getter.getMethod().getReturnType());
        final CryptedStorage an = this.getAnnotation(CryptedStorage.class);
        this.crypted = this.storageHandler.isCrypted();
        this.cryptKey = this.storageHandler.getCryptKey();
        if (an != null) {
            if (this.storageHandler.isCrypted()) {
                throw new InterfaceParseException("No reason to mark " + this + " as @CryptedStorage. Parent is already CryptedStorage");
            } else if (!(this instanceof ListHandler)) { throw new InterfaceParseException(this + " Cannot set @CryptedStorage on primitive fields. Use an object, or an extra plain config interface"); }
            this.crypted = true;
            this.validateEncryptionKey(an.key());
            this.cryptKey = an.key();
        }
        final PlainStorage anplain = this.getAnnotation(PlainStorage.class);
        if (anplain != null) {
            if (an != null) { throw new InterfaceParseException("Cannot Set CryptStorage and PlainStorage Annotation in " + this); }
            if (!this.storageHandler.isCrypted()) {
                throw new InterfaceParseException("No reason to mark " + this + " as @PlainStorage. Parent is already Plain");
            } else if (!(this instanceof ListHandler)) { throw new InterfaceParseException(this + " Cannot set @PlainStorage on primitive fields. Use an object, or an extra plain config interface");
            // primitive storage. cannot set single plain values in a en crypted
            // primitive storage
            }
            // parent crypted, but plain for this single entry
            this.crypted = false;

        }

        final DefaultFactory dv = this.getAnnotation(DefaultFactory.class);
        if (dv != null) {
            this.defaultFactoryClass = dv.value();
        }

        // if rawtypoe is an storable, we can have defaultvalues in
        // json7String from
        final DefaultJsonObject ann = this.getAnnotation(DefaultJsonObject.class);
        if (ann != null) {
            if (this.defaultFactoryClass != null) { throw new InterfaceParseException("Cannot use " + DefaultJsonObject.class.getName() + " and " + DefaultFactory.class.getName() + " for " + this); }
            this.defaultJson = ann.value();
            // this.defaultValue = JSonStorage.restoreFromString(v, typeRef,
            // null);
        }
        try {
            this.validatorFactory = (AbstractValidator<RawClass>) this.getAnnotation(ValidatorFactory.class).value().newInstance();
        } catch (final NullPointerException e) {
        }
        this.checkBadAnnotations(this.getAllowedAnnotations());
        this.initDefaults();

        this.initHandler();

    }

    /**
     * @return
     * 
     */
    @SuppressWarnings("unchecked")
    protected boolean initDefaults() throws Throwable {
        boolean ret = false;
        if (this.defaultFactoryClass != null) {

            this.defaultValue = (RawClass) this.defaultFactoryClass.newInstance().getDefaultValue();
            ret = true;
        }
        if (this.defaultJson != null) {
            this.defaultValue = (RawClass) JSonStorage.restoreFromString(this.defaultJson, new TypeRef<Object>(this.getRawClass()) {
            }, null);
            ret = true;

        }

        final Annotation ann = this.getAnnotation(this.getDefaultAnnotation());
        if (ann != null) {

            this.defaultValue = (RawClass) ann.annotationType().getMethod("value", new Class[] {}).invoke(ann, new Object[] {});
            ret = true;
        }

        return ret;
    }

    /**
     * @throws Throwable
     * 
     */
    protected abstract void initHandler() throws Throwable;

    /**
     * returns true of this keyhandler belongs to ConfigInterface
     * 
     * @param settings
     * @return
     */
    public boolean isChildOf(final ConfigInterface settings) {
        return settings.getStorageHandler() == this.getStorageHandler();
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
     * @param object
     */
    protected abstract void putValue(RawClass object);

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
     * @param newValue
     */
    public void setValue(final RawClass newValue) throws ValidationException {
        try {
            final RawClass oldValue = this.getValue();
            if (oldValue == null && newValue == null) {
                /* everything is null */
                return;
            }
            boolean changed = false;
            if (newValue != null && oldValue == null) {
                /* old is null, but new is not */
                changed = true;
            } else if (oldValue != null && newValue == null) {
                /* new is null, but old is not */
                changed = true;
            } else if (!Clazz.isPrimitive(getRawClass())) {
                /* no primitive, we cannot detect changes 100% */
                changed = true;
            } else if (!newValue.equals(oldValue)) {
                /* does not equal */
                changed = true;
            }
            if (changed == false) { return; }
            if (this.validatorFactory != null) {
                this.validatorFactory.validate(newValue);
            }
            this.validateValue(newValue);
            this.putValue(newValue);

            this.fireEvent(ConfigEvent.Types.VALUE_UPDATED, this, newValue);

        } catch (final ValidationException e) {
            e.setValue(newValue);
            this.fireEvent(ConfigEvent.Types.VALIDATOR_ERROR, this, e);

            throw e;
        } catch (final Throwable t) {
            final ValidationException e = new ValidationException(t);
            e.setValue(newValue);
            this.fireEvent(ConfigEvent.Types.VALIDATOR_ERROR, this, e);

            throw e;
        }

    }

    @Override
    public String toString() {
        return "Keyhandler " + this.storageHandler.getConfigInterface() + "." + this.getKey() + " = " + this.getValue();
    }

    public void validateEncryptionKey(final byte[] key2) {
        if (key2 == null) { throw new InterfaceParseException("Key missing in " + this); }
        if (key2.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + this + " is invalid. required length: " + JSonStorage.KEY.length); }

    }

    /**
     * @param object
     */
    protected abstract void validateValue(RawClass object) throws Throwable;

}
