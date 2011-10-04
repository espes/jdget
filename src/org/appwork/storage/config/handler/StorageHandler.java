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

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

import org.appwork.exceptions.WTFException;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.JsonKeyValueStorage;
import org.appwork.storage.StorageException;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.InterfaceParseException;
import org.appwork.storage.config.MethodHandler;
import org.appwork.storage.config.annotations.AllowStorage;
import org.appwork.storage.config.annotations.CryptedStorage;
import org.appwork.storage.config.events.ConfigEvent;
import org.appwork.storage.config.events.ConfigEventSender;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * @param <T>
 * 
 */
public class StorageHandler<T extends ConfigInterface> implements InvocationHandler {

    private final Class<T>                 configInterface;
    private HashMap<Method, KeyHandler<?>> methodMap;
    private HashMap<String, KeyHandler<?>> keyHandlerMap;

    protected final JsonKeyValueStorage    primitiveStorage;
    private boolean                        crypted;
    private byte[]                         key                  = JSonStorage.KEY;
    private File                           path;
    private ConfigEventSender  eventSender;

    // set externaly to start profiling
    public static HashMap<String, Long>    PROFILER_MAP         = null;
    public static HashMap<String, Long>    PROFILER_CALLNUM_MAP = null;

    /**
     * @param name
     * @param configInterface
     */
    @SuppressWarnings("unchecked")
    public StorageHandler(final File name, final Class<T> configInterface) {
        this.configInterface = configInterface;
        this.eventSender = new ConfigEventSender();

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
        } catch (final Throwable e) {
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

    public ConfigEventSender getEventSender() {
        return this.eventSender;
    }

    public byte[] getKey() {
        return this.key;
    }

    // /**
    // * @return
    // */
    public HashMap<Method, KeyHandler<?>> getMap() {
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
    public Object getPrimitive(final KeyHandler<?> keyHandler) {
        // only evaluate defaults of required
        if (this.primitiveStorage.hasProperty(keyHandler.getKey())) {
            if (Clazz.isBoolean(keyHandler.getRawClass())) {

                return this.getPrimitive(keyHandler.getKey(), false);

            } else if (Clazz.isLong(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), 0l);
            } else if (Clazz.isInteger(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), 0);
            } else if (Clazz.isFloat(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), 0.0f);
            } else if (Clazz.isByte(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), (byte) 0);
            } else if (keyHandler.getRawClass() == String.class) {
                return this.getPrimitive(keyHandler.getKey(), (String) null);
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (keyHandler.getRawClass().isEnum()) {

                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (keyHandler.getRawClass() == Double.class | keyHandler.getRawClass() == double.class) {
                return this.getPrimitive(keyHandler.getKey(), 0.0d);
            } else {
                throw new StorageException("Invalid datatype: " + keyHandler.getRawClass());
            }
        } else {
            if (Clazz.isBoolean(keyHandler.getRawClass())) {

                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());

            } else if (Clazz.isLong(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (Clazz.isInteger(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (Clazz.isFloat(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (Clazz.isByte(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (keyHandler.getRawClass() == String.class) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
                // } else if (getter.getRawClass() == String[].class) {
                // return this.get(getter.getKey(),
                // getter.getDefaultStringArray());
            } else if (keyHandler.getRawClass().isEnum()) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
            } else if (Clazz.isDouble(keyHandler.getRawClass())) {
                return this.getPrimitive(keyHandler.getKey(), keyHandler.getDefaultValue());
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

    @SuppressWarnings("unchecked")
    public Object invoke(final Object instance, final Method m, final Object[] parameter) throws Throwable {
        long t = PROFILER_MAP == null ? 0 : System.nanoTime();

        try {

            if (m == null) {
                // yes.... Method m may be null. this happens if we call a
                // method in the interface's own static init.
                return this;
            }
            final KeyHandler<?> handler = this.methodMap.get(m);
            if (handler != null) {
                if (handler.isGetter(m)) {
                    return handler.getValue();

                } else {
                    ((KeyHandler<Object>)handler).setValue(parameter[0]);
                  

                    return null;
                }
            } else if (m.getName().equals("toString")) {
                return this.toString();
                // } else if (m.getName().equals("addListener")) {
                // this.eventSender.addListener((ConfigEventListener)
                // parameter[0]);
                // return null;
                // } else if (m.getName().equals("removeListener")) {
                // this.eventSender.removeListener((ConfigEventListener)
                // parameter[0]);
                // return null;
            } else if (m.getName().equals("getStorageHandler")) {
                return this;

            } else {
                throw new WTFException(m + " ??? no keyhandler. This is not possible!");
            }
        } finally {
            if (PROFILER_MAP != null && m != null) {
                long dur = System.nanoTime() - t;
                String id = m.toString();
                Long g = PROFILER_MAP.get(id);
                if (g == null) g = 0l;
                PROFILER_MAP.put(id, g + dur);
            }
            if (PROFILER_CALLNUM_MAP != null && m != null) {
                String id = m.toString();
                Long g = PROFILER_CALLNUM_MAP.get(id);
                PROFILER_CALLNUM_MAP.put(id, g == null ? 1 : g + 1);
            }

        }
    }

    public boolean isCrypted() {
        return this.crypted;
    }

    /**
     * @throws Throwable
     * 
     */
    private void parseInterface() throws Throwable {
        this.methodMap = new HashMap<Method, KeyHandler<?>>();
        this.keyHandlerMap = new HashMap<String, KeyHandler<?>>();
        final HashMap<String, Method> keyGetterMap = new HashMap<String, Method>();
        final HashMap<String, Method> keySetterMap = new HashMap<String, Method>();
        String key;
        final HashMap<String, KeyHandler<?>> parseMap = new HashMap<String, KeyHandler<?>>();

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
                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = createKeyHandler(key, m.getGenericReturnType());
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

                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = createKeyHandler(key, m.getGenericReturnType());
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

                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = createKeyHandler(key, m.getGenericParameterTypes()[0]);
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

        for (final KeyHandler<?> kh : this.methodMap.values()) {
            kh.init();
        }
    }

    /**
     * @param key2
     * @param genericReturnType
     * @return
     */
    private KeyHandler<?> createKeyHandler(String key, Type type) {
        if (type instanceof Class && ((Class<?>) type).isArray()) {

            final Class<?> ct = ((Class<?>) type).getComponentType();
            if (Clazz.isBoolean(ct)) {
                return new BooleanListHandler(this, key);

            } else if (Clazz.isLong(ct)) {

                return new LongListHandler(this, key);
            } else if (Clazz.isInteger(ct)) {
                return new IntegerListHandler(this, key);
            } else if (Clazz.isByte(ct)) {
                return new ByteListHandler(this, key);
            } else if (Clazz.isFloat(ct)) {

                return new FloatListHandler(this, key);
            } else if (ct == String.class) {
                return new StringListHandler(this, key);
            } else if (ct.isEnum()) {
                return new EnumListHandler(this, key);

            } else if (Clazz.isDouble(ct)) {
                return new DoubleListHandler(this, key);
            } else {

                return new ObjectKeyHandler(this, key, type);
            }
        } else if (Clazz.isBoolean(type)) {
            return new BooleanKeyHandler(this, key);
        } else if (Clazz.isByte(type)) {
            return new ByteKeyHandler(this, key);
        } else if (Clazz.isDouble(type)) {
            return new DoubleKeyHandler(this, key);
        } else if (Clazz.isFloat(type)) {
            return new FloatKeyHandler(this, key);

        } else if (Clazz.isInteger(type)) {
            return new IntegerKeyHandler(this, key);
        } else if (type instanceof Class && ((Class<?>) type).isEnum()) {
            return new EnumKeyHandler(this, key);
        } else if (type == String.class) {
            return new StringKeyHandler(this, key);
        } else if (Clazz.isLong(type)) {
            return new LongKeyHandler(this, key);

        } else {

            return new ObjectKeyHandler(this, key, type);
        }
    }

    /**
     * @param key
     * @param object
     */
    protected void putPrimitive(final String key, final Boolean value) {
        this.primitiveStorage.put(key, value);

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Byte object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Double object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Enum<?> object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Float object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Integer object) {
        this.primitiveStorage.put(key2, object);

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Long object) {
        this.primitiveStorage.put(key2, object);
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final String object) {
        this.primitiveStorage.put(key2, object);

    }

    @Override
    public String toString() {
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        for (final KeyHandler<?> h : this.methodMap.values()) {

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
    @SuppressWarnings("unchecked")
    public KeyHandler<Object> getKeyHandler(String key) {
        return getKeyHandler(key,KeyHandler.class);

    }

   

    protected void fireEvent(ConfigEvent.Types type, KeyHandler<?> keyHandler, Object parameter) {
        eventSender.fireEvent(new ConfigEvent(type, keyHandler, parameter));
    }



    /**
     * @throws NullPointerException
     *             if there is no keyhandler for key
     * @param string
     * @return
     */

    public Object getValue(String key) {

        return getKeyHandler(key).getValue();
    }

    /**
     * @param <RawClass>
     * @param string
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends KeyHandler<?>>E getKeyHandler(String key, Class<E> class1) {
        KeyHandler<?> ret = keyHandlerMap.get(key.toLowerCase(Locale.ENGLISH));
        if (ret == null) throw new NullPointerException("No KeyHandler: " + key + " in " + configInterface);
        return (E) ret;
    }

}
