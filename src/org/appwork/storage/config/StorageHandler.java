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

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.JsonKeyValueStorage;
import org.appwork.storage.StorageException;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.annotations.DefaultFactory;

/**
 * @author thomas
 * @param <T>
 * 
 */
public class StorageHandler<T extends ConfigInterface> implements InvocationHandler {

    private final Class<T>                configInterface;
    private HashMap<Method, KeyHandler>   methodMap;

    private final JsonKeyValueStorage     primitiveStorage;
    private boolean                       crypted;
    private byte[]                        key = JSonStorage.KEY;
    private File                          path;
    private ConfigInterfaceEventSender<T> eventSender;
    private T                             defaultFactory;

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
     * @param getter
     * @return
     */
    public Object getPrimitive(final MethodHandler getter) {
        // only evaluate defaults of required
        if (this.primitiveStorage.hasProperty(getter.getKey())) {
            if (getter.getRawClass() == Boolean.class || getter.getRawClass() == boolean.class) {

                return this.getPrimitive(getter.getKey(), false);

            } else if (getter.getRawClass() == Long.class || getter.getRawClass() == long.class) {
                return this.getPrimitive(getter.getKey(), 0l);
            } else if (getter.getRawClass() == Integer.class || getter.getRawClass() == int.class) {
                return this.getPrimitive(getter.getKey(), 0);
            } else if (getter.getRawClass() == Float.class || getter.getRawClass() == float.class) {
                return this.getPrimitive(getter.getKey(), 0.0f);
            } else if (getter.getRawClass() == Byte.class || getter.getRawClass() == byte.class) {
                return this.getPrimitive(getter.getKey(), (byte) 0);
            } else if (getter.getRawClass() == String.class) {
                return this.getPrimitive(getter.getKey(), (String) null);
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (getter.getRawClass().isEnum()) {

                return this.getPrimitive(getter.getKey(), getter.getDefaultEnum());
            } else if (getter.getRawClass() == Double.class | getter.getRawClass() == double.class) {
                return this.getPrimitive(getter.getKey(), 0.0d);
            } else {
                throw new StorageException("Invalid datatype: " + getter.getRawClass());
            }
        } else {
            if (getter.getRawClass() == Boolean.class || getter.getRawClass() == boolean.class) {

                return this.getPrimitive(getter.getKey(), getter.getDefaultBoolean());

            } else if (getter.getRawClass() == Long.class || getter.getRawClass() == long.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultLong());
            } else if (getter.getRawClass() == Integer.class || getter.getRawClass() == int.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultInteger());
            } else if (getter.getRawClass() == Float.class || getter.getRawClass() == float.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultFloat());
            } else if (getter.getRawClass() == Byte.class || getter.getRawClass() == byte.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultByte());
            } else if (getter.getRawClass() == String.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultString());
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (getter.getRawClass().isEnum()) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultEnum());
            } else if (getter.getRawClass() == Double.class | getter.getRawClass() == double.class) {
                return this.getPrimitive(getter.getKey(), getter.getDefaultDouble());
            } else {
                throw new StorageException("Invalid datatype: " + getter.getRawClass());
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

    @SuppressWarnings("unchecked")
    public Object invoke(final Object instance, final Method m, final Object[] parameter) throws Throwable {
        if (m.getName().equals("toString")) {
            return this.toString();
        } else if (m.getName().equals("addListener")) {
            this.eventSender.addListener((ConfigEventListener) parameter[0]);
            return null;
        } else if (m.getName().equals("removeListener")) {
            this.eventSender.removeListener((ConfigEventListener) parameter[0]);
            return null;
        } else if (m.getName().equals("getStorageHandler")) {
            return this;

        } else {
            final KeyHandler handler = this.methodMap.get(m);
            if (handler.isGetter(m)) {
                return handler.getValue();

            } else {
                handler.setValue(parameter[0]);
                this.eventSender.fireEvent(new ConfigEvent<T>((T) instance, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
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

        final HashMap<String, Method> keyGetterMap = new HashMap<String, Method>();
        final HashMap<String, Method> keySetterMap = new HashMap<String, Method>();
        String key;
        final HashMap<String, KeyHandler> parseMap = new HashMap<String, KeyHandler>();

        Class<?> clazz = this.configInterface;
        while (clazz != null && clazz != ConfigInterface.class) {
            for (final Method m : clazz.getDeclaredMethods()) {
                if (m.getName().startsWith("get")) {
                    key = m.getName().substring(3).toLowerCase();
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
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m, JSonStorage.canStorePrimitive(m.getReturnType()));
                    kh.setGetter(h);

                    this.methodMap.put(m, kh);

                } else if (m.getName().startsWith("is")) {
                    key = m.getName().substring(2).toLowerCase();
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
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m, JSonStorage.canStorePrimitive(m.getReturnType()));
                    kh.setGetter(h);

                    this.methodMap.put(m, kh);
                } else if (m.getName().startsWith("set")) {
                    key = m.getName().substring(3).toLowerCase();
                    if (keySetterMap.containsKey(key)) { throw new InterfaceParseException("Key " + key + " Dupe found! " + keySetterMap.containsKey(key) + "<-->" + m); }
                    if (m.getParameterTypes().length != 1) { throw new InterfaceParseException("Setter " + m + " has !=1 parameters."); }
                    if (m.getReturnType() != void.class) { throw new InterfaceParseException("Setter " + m + " has a returntype != void"); }
                    try {
                        JSonStorage.canStore(m.getGenericParameterTypes()[0]);
                    } catch (final InvalidTypeException e) {
                        throw new InterfaceParseException(e);
                    }

                    KeyHandler kh = parseMap.get(key);
                    if (kh == null) {
                        kh = new KeyHandler(this, key);
                        parseMap.put(key, kh);
                    }
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.SETTER, key, m, JSonStorage.canStorePrimitive(m.getParameterTypes()[0]));
                    kh.setSetter(h);

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
}
