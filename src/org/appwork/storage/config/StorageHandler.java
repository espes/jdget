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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.JsonKeyValueStorage;
import org.appwork.storage.StorageException;
import org.appwork.storage.config.annotations.AllowStorage;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultFactory;
import org.appwork.storage.config.annotations.ValueValidator;

/**
 * @author thomas
 * @param <T>
 * 
 */
public class StorageHandler<T extends ConfigInterface> implements InvocationHandler {

    private final Class<T>                configInterface;
    private HashMap<Method, KeyHandler>   methodMap;
    private HashMap<String, KeyHandler>   keyHandlerMap;

    private final JsonKeyValueStorage     primitiveStorage;
    private boolean                       crypted;
    private byte[]                        key = JSonStorage.KEY;
    private File                          path;
    private ConfigInterfaceEventSender<T> eventSender;
    private T                             defaultFactory;
    private T                             validator;

    /**
     * @param name
     * @param configInterface
     */
    @SuppressWarnings("unchecked")
    public StorageHandler(final File name, final Class<T> configInterface) {
        this.configInterface = configInterface;
        this.eventSender = new ConfigInterfaceEventSender<T>();
        final DefaultFactory defaultFactoryAnnotation = configInterface.getAnnotation(DefaultFactory.class);
        if (defaultFactoryAnnotation != null && defaultFactoryAnnotation.value() != null) {
            try {
                final Class<? extends ConfigInterface> clazz = defaultFactoryAnnotation.value();
                for (final Class<?> c : clazz.getInterfaces()) {
                    if (c == configInterface) {
                        this.defaultFactory = (T) defaultFactoryAnnotation.value().newInstance();
                        break;
                    }

                }
                if (this.defaultFactory == null) {

                throw new InterfaceParseException("Defaultfactory " + clazz + " has to implement " + configInterface); }

            } catch (final InstantiationException e) {
                throw new InterfaceParseException(e);
            } catch (final IllegalAccessException e) {
                throw new InterfaceParseException(e);
            }
        }

        final ValueValidator validator = configInterface.getAnnotation(ValueValidator.class);
        if (validator != null && validator.value() != null) {
            try {
                final Class<? extends ConfigInterface> clazz = validator.value();
                for (final Class<?> c : clazz.getInterfaces()) {
                    if (c == configInterface) {
                        this.validator = (T) validator.value().newInstance();
                        break;
                    }

                }
                if (this.validator == null) {

                throw new InterfaceParseException("ValueValidator " + clazz + " has to implement " + configInterface); }

            } catch (final InstantiationException e) {
                throw new InterfaceParseException(e);
            } catch (final IllegalAccessException e) {
                throw new InterfaceParseException(e);
            }
        }

        this.path = name;
        final CryptedStorage crypted = configInterface.getAnnotation(CryptedStorage.class);
        if (crypted != null) {
            this.crypted = true;
            if (crypted.key() != null) {
                this.primitiveStorage = new JsonKeyValueStorage(new File(this.path.getAbsolutePath() + ".ejs"), false, crypted.key());

                this.key = crypted.key();
                if (this.key.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + configInterface + " is invalid"); }

            } else {

                this.primitiveStorage = new JsonKeyValueStorage(new File(this.path.getAbsolutePath() + ".ejs"), false, this.key = JSonStorage.KEY);

            }
        } else {
            this.crypted = false;
            this.primitiveStorage = new JsonKeyValueStorage(new File(this.path.getAbsolutePath() + ".json"), true);
        }
        try {
            this.parseInterface();
        } catch (final Exception e) {
            throw new InterfaceParseException(e);
        }
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public void run() {
                StorageHandler.this.primitiveStorage.save();
            }

            @Override
            public String toString() {
                return "Save " + StorageHandler.this.path + "[" + configInterface.getName() + "]";
            }
        });
    }

    public Class<T> getConfigInterface() {
        return this.configInterface;
    }

    public T getDefaultFactory() {
        return this.defaultFactory;
    }

    public ConfigInterfaceEventSender<T> getEventSender() {
        return this.eventSender;
    }

    public byte[] getKey() {
        return this.key;
    }

    /**
     * @return
     */
    public HashMap<Method, KeyHandler> getMap() {
        // TODO Auto-generated method stub
        return this.methodMap;
    }

    /**
     * @return
     */
    public File getPath() {
        return this.path;
    }

    /**
     * @param keyHandler
     * @return
     */
    public Object getPrimitive(final KeyHandler keyHandler) {
        // only evaluate defaults of required
        if (this.primitiveStorage.hasProperty(keyHandler.getKey())) {
            if (keyHandler.getRawClass() == Boolean.class || keyHandler.getRawClass() == boolean.class) {

                return this.getPrimitive(keyHandler.getKey(), false);

            } else if (keyHandler.getRawClass() == Long.class || keyHandler.getRawClass() == long.class) {
                return this.getPrimitive(keyHandler.getKey(), 0l);
            } else if (keyHandler.getRawClass() == Integer.class || keyHandler.getRawClass() == int.class) {
                return this.getPrimitive(keyHandler.getKey(), 0);
            } else if (keyHandler.getRawClass() == Float.class || keyHandler.getRawClass() == float.class) {
                return this.getPrimitive(keyHandler.getKey(), 0.0f);
            } else if (keyHandler.getRawClass() == Byte.class || keyHandler.getRawClass() == byte.class) {
                return this.getPrimitive(keyHandler.getKey(), (byte) 0);
            } else if (keyHandler.getRawClass() == String.class) {
                return this.getPrimitive(keyHandler.getKey(), (String) null);
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (keyHandler.getRawClass().isEnum()) {

                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultEnum());
            } else if (keyHandler.getRawClass() == Double.class | keyHandler.getRawClass() == double.class) {
                return this.getPrimitive(keyHandler.getKey(), 0.0d);
            } else {
                throw new StorageException("Invalid datatype: " + keyHandler.getRawClass());
            }
        } else {
            if (keyHandler.getRawClass() == Boolean.class || keyHandler.getRawClass() == boolean.class) {

                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultBoolean());

            } else if (keyHandler.getRawClass() == Long.class || keyHandler.getRawClass() == long.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultLong());
            } else if (keyHandler.getRawClass() == Integer.class || keyHandler.getRawClass() == int.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultInteger());
            } else if (keyHandler.getRawClass() == Float.class || keyHandler.getRawClass() == float.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultFloat());
            } else if (keyHandler.getRawClass() == Byte.class || keyHandler.getRawClass() == byte.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultByte());
            } else if (keyHandler.getRawClass() == String.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultString());
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (keyHandler.getRawClass().isEnum()) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultEnum());
            } else if (keyHandler.getRawClass() == Double.class | keyHandler.getRawClass() == double.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultDouble());
            } else {
                throw new StorageException("Invalid datatype: " + keyHandler.getRawClass());
            }
        }
    }

    /**
     * @param <E>
     * @param key2
     * @param defaultBoolean
     * @return
     */
    public <E> E getPrimitive(final String key, final E def) {

        return this.primitiveStorage.get(key, def);
    }

    public T getValidator() {
        return this.validator;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(final Object instance, final Method m, final Object[] parameter) throws Throwable {
        if (m.getName().equals("toString")) {
            return this.toString();
            // } else if (m.getName().equals("addListener")) {
            // this.eventSender.addListener((ConfigEventListener) parameter[0]);
            // return null;
            // } else if (m.getName().equals("removeListener")) {
            // this.eventSender.removeListener((ConfigEventListener)
            // parameter[0]);
            // return null;
        } else if (m.getName().equals("getStorageHandler")) {
            return this;

        } else {
            final KeyHandler handler = this.methodMap.get(m);
            if (handler.isGetter(m)) {
                return handler.getValue();

            } else {
                setValue(handler, parameter[0]);

                return null;
            }
        }

    }

    public boolean isCrypted() {
        return this.crypted;
    }

    /**
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * 
     */
    private void parseInterface() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        this.methodMap = new HashMap<Method, KeyHandler>();
        this.keyHandlerMap = new HashMap<String, KeyHandler>();
        final HashMap<String, Method> keyGetterMap = new HashMap<String, Method>();
        final HashMap<String, Method> keySetterMap = new HashMap<String, Method>();
        String key;
        final HashMap<String, KeyHandler> parseMap = new HashMap<String, KeyHandler>();

        Class<?> clazz = this.configInterface;
        while (clazz != null && clazz != ConfigInterface.class) {
            for (final Method m : clazz.getDeclaredMethods()) {
                if (m.getName().startsWith("get")) {
                    key = m.getName().substring(3).toLowerCase(Locale.ENGLISH);
                    // we do not allow to setters/getters with the same name but
                    // different cases. this only confuses the user when editing
                    // the
                    // later config file
                    if (keyGetterMap.containsKey(key)) { throw new InterfaceParseException("Key " + key + " Dupe found! " + keyGetterMap.containsKey(key) + "<-->" + m); }

                    if (m.getParameterTypes().length > 0) { throw new InterfaceParseException("Getter " + m + " has parameters."); }
                    try {
                        JSonStorage.canStore(m.getGenericReturnType());
                    } catch (final InvalidTypeException e) {
                        final AllowStorage allow = m.getAnnotation(AllowStorage.class);
                        boolean found = false;
                        if (allow != null) {
                            for (final Class<?> c : allow.value()) {
                                if (e.getType() == c) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) { throw new InterfaceParseException(e); }
                    }
                    KeyHandler kh = parseMap.get(key);
                    if (kh == null) {
                        kh = new KeyHandler(this, key);
                        parseMap.put(key, kh);
                    }
                    // JSonStorage.canStorePrimitive(m.getReturnType())
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m);
                    kh.setGetter(h);

                    this.methodMap.put(m, kh);
                    keyHandlerMap.put(key, kh);

                } else if (m.getName().startsWith("is")) {
                    key = m.getName().substring(2).toLowerCase(Locale.ENGLISH);
                    // we do not allow to setters/getters with the same name but
                    // different cases. this only confuses the user when editing
                    // the
                    // later config file
                    if (keyGetterMap.containsKey(key)) { throw new InterfaceParseException("Key " + key + " Dupe found! " + keyGetterMap.containsKey(key) + "<-->" + m); }

                    if (m.getParameterTypes().length > 0) { throw new InterfaceParseException("Getter " + m + " has parameters."); }
                    try {
                        JSonStorage.canStore(m.getGenericReturnType());
                    } catch (final InvalidTypeException e) {
                        throw new InterfaceParseException(e);
                    }

                    KeyHandler kh = parseMap.get(key);
                    if (kh == null) {
                        kh = new KeyHandler(this, key);
                        parseMap.put(key, kh);
                    }
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m);
                    kh.setGetter(h);
                    keyHandlerMap.put(key, kh);
                    this.methodMap.put(m, kh);
                } else if (m.getName().startsWith("set")) {
                    key = m.getName().substring(3).toLowerCase(Locale.ENGLISH);
                    if (keySetterMap.containsKey(key)) { throw new InterfaceParseException("Key " + key + " Dupe found! " + keySetterMap.containsKey(key) + "<-->" + m); }
                    if (m.getParameterTypes().length != 1) { throw new InterfaceParseException("Setter " + m + " has !=1 parameters."); }
                    if (m.getReturnType() != void.class) { throw new InterfaceParseException("Setter " + m + " has a returntype != void"); }
                    try {
                        JSonStorage.canStore(m.getGenericParameterTypes()[0]);
                    } catch (final InvalidTypeException e) {
                        final AllowStorage allow = m.getAnnotation(AllowStorage.class);
                        boolean found = false;
                        if (allow != null) {
                            for (final Class<?> c : allow.value()) {
                                if (e.getType() == c) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) { throw new InterfaceParseException(e); }
                    }

                    KeyHandler kh = parseMap.get(key);
                    if (kh == null) {
                        kh = new KeyHandler(this, key);
                        parseMap.put(key, kh);
                    }
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.SETTER, key, m);
                    kh.setSetter(h);
                    keyHandlerMap.put(key, kh);
                    this.methodMap.put(m, kh);

                } else {
                    throw new InterfaceParseException("Only getter and setter allowed:" + m);

                }
            }
            // run down the calss hirarchy to find all methods. getMethods does
            // not work, because it only finds public methods
            final Class<?>[] interfaces = clazz.getInterfaces();
            clazz = interfaces[0];

        }

        for (final KeyHandler kh : this.methodMap.values()) {
            kh.init();
        }
    }

    /**
     * @param key
     * @param object
     */
    public void putPrimitive(final String key, final Boolean value) {
        this.primitiveStorage.put(key, value);

    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Byte object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Double object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Enum<?> object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Float object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Integer object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final Long object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    public void putPrimitive(final String key2, final String object) {
        this.primitiveStorage.put(key2, object);

    }

    @Override
    public String toString() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        for (final KeyHandler h : this.methodMap.values()) {

            try {
                ret.put(h.getGetter().getKey(), this.invoke(null, h.getGetter().getMethod(), new Object[] {}));
            } catch (final Throwable e) {
                e.printStackTrace();
                ret.put(h.getKey(), e.getMessage());
            }

        }
        return JSonStorage.toString(ret);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */

    /**
     * @param key2
     */
    public KeyHandler getKeyHandler(String key) {
        return keyHandlerMap.get(key.toLowerCase(Locale.ENGLISH));

    }

    /**
     * @param keyHandler
     * @param parameter
     */
    public void setValue(KeyHandler handler, Object parameter) {
        try {
            handler.setValue(parameter);
            this.eventSender.fireEvent(new ConfigEvent<T>(getConfigInterface(), ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter));
        } catch (final InvocationTargetException t) {
            this.eventSender.fireEvent(new ConfigEvent<T>(getConfigInterface(), ConfigEvent.Types.VALIDATOR_ERROR, t.getTargetException(), handler));

        } catch (final Throwable t) {
            this.eventSender.fireEvent(new ConfigEvent<T>(getConfigInterface(), ConfigEvent.Types.VALIDATOR_ERROR, t, handler));

        }
    }

    /**
     * @param keyHandler
     * @return
     */
    public Object getValue(KeyHandler keyHandler) {
     
        return keyHandler.getValue();
    }

}
