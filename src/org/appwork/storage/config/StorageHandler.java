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

/**
 * @author thomas
 * @param <T>
 * 
 */
public class StorageHandler<T extends ConfigInterface> implements InvocationHandler {

    private final Class<T>                 configInterface;
    private HashMap<Method, MethodHandler> getterMap;

    private final JsonKeyValueStorage      primitiveStorage;
    private boolean                        crypted;
    private byte[]                         key = JSonStorage.KEY;
    private File                           path;
    private ConfigInterfaceEventSender<T>  eventSender;

    /**
     * @param name
     * @param configInterface
     */
    public StorageHandler(final File name, final Class<T> configInterface) {
        this.configInterface = configInterface;
        this.eventSender = new ConfigInterfaceEventSender<T>();

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

    public byte[] getKey() {
        return this.key;
    }

    /**
     * @return
     */
    public HashMap<Method, MethodHandler> getMap() {
        // TODO Auto-generated method stub
        return this.getterMap;
    }

    /**
     * @return
     */
    public File getPath() {
        return this.path;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(final Object arg0, final Method m, final Object[] parameter) throws Throwable {
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
            final MethodHandler handler = this.getterMap.get(m);
            if (handler.isGetter()) {
                if (handler.isPrimitive()) {
                    if (handler.getRawClass() == Boolean.class || handler.getRawClass() == boolean.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultBoolean());
                    } else if (handler.getRawClass() == Long.class || handler.getRawClass() == long.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultLong());
                    } else if (handler.getRawClass() == Integer.class || handler.getRawClass() == int.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultInteger());
                    } else if (handler.getRawClass() == Float.class || handler.getRawClass() == float.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultFloat());
                    } else if (handler.getRawClass() == Byte.class || handler.getRawClass() == byte.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultByte());
                    } else if (handler.getRawClass() == String.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultString());
                        // } else if (handler.getRawClass() == String[].class) {
                        // return this.primitiveStorage.get(handler.getKey(),
                        // handler.getDefaultStringArray());
                    } else if (handler.getRawClass().isEnum()) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultEnum());
                    } else if (handler.getRawClass() == Double.class | handler.getRawClass() == double.class) {
                        return this.primitiveStorage.get(handler.getKey(), handler.getDefaultDouble());
                    } else {
                        throw new StorageException("Invalid datatype: " + handler.getRawClass());
                    }

                } else {
                    return handler.read();
                }

            } else {
                if (handler.isPrimitive()) {

                    if (handler.getRawClass() == Boolean.class || handler.getRawClass() == boolean.class) {
                        this.primitiveStorage.put(handler.getKey(), (Boolean) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == Long.class || handler.getRawClass() == long.class) {
                        this.primitiveStorage.put(handler.getKey(), (Long) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == Integer.class || handler.getRawClass() == int.class) {
                        this.primitiveStorage.put(handler.getKey(), (Integer) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == Float.class || handler.getRawClass() == float.class) {
                        this.primitiveStorage.put(handler.getKey(), (Float) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == Byte.class || handler.getRawClass() == byte.class) {
                        this.primitiveStorage.put(handler.getKey(), (Byte) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == String.class) {
                        this.primitiveStorage.put(handler.getKey(), (String) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                        // } else if (handler.getRawClass() == String[].class) {
                        // this.primitiveStorage.put(handler.getKey(),
                        // (String[]) parameter);
                    } else if (handler.getRawClass().isEnum()) {
                        this.primitiveStorage.put(handler.getKey(), (Enum<?>) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else if (handler.getRawClass() == Double.class || handler.getRawClass() == double.class) {
                        this.primitiveStorage.put(handler.getKey(), (Double) parameter[0]);
                        this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    } else {
                        throw new StorageException("Invalid datatype: " + handler.getRawClass());
                    }

                    return null;
                } else {
                    handler.write(parameter[0]);

                    this.eventSender.fireEvent(new ConfigEvent<T>((T) arg0, ConfigEvent.Types.VALUE_UPDATED, handler.getKey(), parameter[0]));
                    return null;
                }
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
        this.getterMap = new HashMap<Method, MethodHandler>();

        final HashMap<String, Method> keyGetterMap = new HashMap<String, Method>();
        final HashMap<String, Method> keySetterMap = new HashMap<String, Method>();
        String key;

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

                    MethodHandler h;
                    this.getterMap.put(m, h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m, JSonStorage.canStorePrimitive(m.getReturnType())));
                    keyGetterMap.put(key, m);
                    final MethodHandler setterhandler = this.getterMap.get(keySetterMap.get(key));
                    if (setterhandler != null) {
                        setterhandler.setGetter(h);
                        h.setSetter(setterhandler);
                    }
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

                    MethodHandler h;
                    this.getterMap.put(m, h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m, JSonStorage.canStorePrimitive(m.getReturnType())));
                    keyGetterMap.put(key, m);
                    final MethodHandler setterhandler = this.getterMap.get(keySetterMap.get(key));
                    if (setterhandler != null) {
                        setterhandler.setGetter(h);
                        h.setSetter(setterhandler);
                    }
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
                    MethodHandler h;
                    this.getterMap.put(m, h = new MethodHandler(this, MethodHandler.Type.SETTER, key, m, JSonStorage.canStorePrimitive(m.getParameterTypes()[0])));

                    keySetterMap.put(key, m);

                    final MethodHandler getterHandler = this.getterMap.get(keyGetterMap.get(key));
                    if (getterHandler != null) {
                        getterHandler.setSetter(h);
                        h.setGetter(getterHandler);
                    }
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

    @Override
    public String toString() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        for (final MethodHandler h : this.getterMap.values()) {
            if (h.getType() == MethodHandler.Type.GETTER) {
                try {
                    ret.put(h.getKey(), this.invoke(null, h.getMethod(), new Object[] {}));
                } catch (final Throwable e) {
                    e.printStackTrace();
                    ret.put(h.getKey(), e.getMessage());
                }
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
