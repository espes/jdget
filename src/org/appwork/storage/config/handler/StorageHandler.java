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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.appwork.exceptions.WTFException;
import org.appwork.scheduler.DelayedRunnable;
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
import org.appwork.storage.config.annotations.DefaultBooleanArrayValue;
import org.appwork.storage.config.annotations.DefaultByteArrayValue;
import org.appwork.storage.config.annotations.DefaultDoubleArrayValue;
import org.appwork.storage.config.annotations.DefaultFloatArrayValue;
import org.appwork.storage.config.annotations.DefaultIntArrayValue;
import org.appwork.storage.config.annotations.DefaultLongArrayValue;
import org.appwork.storage.config.events.ConfigEvent;
import org.appwork.storage.config.events.ConfigEventSender;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.appwork.utils.reflection.Clazz;
import org.appwork.utils.swing.dialog.Dialog;

/**
 * @author thomas
 * @param <T>
 * 
 */
public class StorageHandler<T extends ConfigInterface> implements InvocationHandler {
    public final static ScheduledThreadPoolExecutor TIMINGQUEUE            = new ScheduledThreadPoolExecutor(1);
    static {
        TIMINGQUEUE.setKeepAliveTime(30000, TimeUnit.MILLISECONDS);
    }
    private final Class<T>                          configInterface;
    private HashMap<Method, KeyHandler<?>>          methodMap;
    private HashMap<String, KeyHandler<?>>          keyHandlerMap;

    protected final JsonKeyValueStorage             primitiveStorage;
    private boolean                                 crypted;

    private byte[]                                  key                    = JSonStorage.KEY;
    private File                                    path;
    private ConfigEventSender<Object>               eventSender;
    private String                                  relativCPPath;
    protected boolean                               save                   = true;
    private DelayedRunnable                         delayedSaver;
    private long                                    delayedSaveMaxInterval = 5 * 60 * 1000;

    /**
     * The Keyvalue storage in this handler can write itself to disk based on an
     * interval.
     * 
     * <br>
     * we write to disk {@link #getDelayedSaveInterval()} MS after the last
     * change, but at least {@link #getDelayedSaveMaxInterval()} after a change
     * 
     * @param delayedSaveMaxInterval
     */
    public void setDelayedSaveMaxInterval(long delayedSaveMaxInterval) {
        this.delayedSaveMaxInterval = delayedSaveMaxInterval;
    }

    // set externaly to start profiling
    public static HashMap<String, Long> PROFILER_MAP         = null;

    public static HashMap<String, Long> PROFILER_CALLNUM_MAP = null;

    public void disableSaveAtEnd() {
        save = false;
    }

    /**
     * @param name
     * @param configInterface
     */
    public StorageHandler(final File name, final Class<T> configInterface) {
        this.configInterface = configInterface;
        this.eventSender = new ConfigEventSender<Object>();

        this.path = name;
        if (name.getName().endsWith(".json") || name.getName().endsWith(".ejs")) {
            Log.L.warning(name + " should not have an extension!!");
        }
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
        } catch (final InterfaceParseException e) {
            throw e;
        } catch (final Throwable e) {
            throw new InterfaceParseException(e);
        }
        Log.L.finer("Load Storage: " + this.path);
        updateSaveDelayer();
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public int getHookPriority() {
                return 0;
            }

            @Override
            public void run() {
                if (save) StorageHandler.this.primitiveStorage.save();
            }

            @Override
            public String toString() {
                return "Save " + StorageHandler.this.path + "[" + configInterface.getName() + "]";
            }
        });

    }

    /**
     * @param path2
     * @param configInterface2
     * @throws URISyntaxException
     */
    public StorageHandler(final String classPath, final Class<T> configInterface) throws URISyntaxException {
        this.configInterface = configInterface;
        this.eventSender = new ConfigEventSender<Object>();

        this.relativCPPath = classPath;
        this.path = Application.getResource(classPath);

        if (this.path.getName().endsWith(".json") || this.path.getName().endsWith(".ejs")) {
            Log.L.warning(classPath + " should not have an extension!!");
        }
        final CryptedStorage crypted = configInterface.getAnnotation(CryptedStorage.class);
        if (crypted != null) {
            this.crypted = true;
            if (crypted.key() != null) {
                this.path = new File(Application.class.getClassLoader().getResource(classPath + ".ejs").toURI());
                this.primitiveStorage = new JsonKeyValueStorage(Application.getResource(classPath + ".ejs"), Application.class.getClassLoader().getResource(classPath + ".ejs"), false, crypted.key());

                this.key = crypted.key();
                if (this.key.length != JSonStorage.KEY.length) { throw new InterfaceParseException("Crypt key for " + configInterface + " is invalid"); }

            } else {
                this.path = new File(Application.class.getClassLoader().getResource(classPath + ".ejs").toURI());
                this.primitiveStorage = new JsonKeyValueStorage(Application.getResource(classPath + ".ejs"), Application.class.getClassLoader().getResource(classPath + ".ejs"), false, this.key = JSonStorage.KEY);

            }
        } else {
            this.crypted = false;
            this.primitiveStorage = new JsonKeyValueStorage(Application.getResource(classPath + ".json"), Application.class.getClassLoader().getResource(classPath + ".json"), true, null);
        }

        try {
            this.parseInterface();
        } catch (final Throwable e) {
            throw new InterfaceParseException(e);
        }
        Log.L.finer("Load Storage: " + this.path);

        updateSaveDelayer();

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public int getHookPriority() {
                return 0;
            }

            @Override
            public void run() {
                if (save) StorageHandler.this.primitiveStorage.save();
            }

            @Override
            public String toString() {
                return "Save " + StorageHandler.this.path + "[" + configInterface.getName() + "]";
            }
        });

    }

    /**
     * 
     */
    protected void updateSaveDelayer() {
        synchronized (this) {
            final int interval = getDelayedSaveInterval();
            long maxInterval = getDelayedSaveMaxInterval();
            if (interval < 0) {
                delayedSaver = null;
            } else {

                delayedSaver = new DelayedRunnable(TIMINGQUEUE, interval, maxInterval) {

                    @Override
                    public void delayedrun() {
                        write();
                    }
                };
            }
        }
    }

    /**
     * @return
     */
    public long getDelayedSaveMaxInterval() {
        // TODO Auto-generated method stub
        return delayedSaveMaxInterval;
    }

    public void write() {
        Log.L.info("Save " + configInterface);
        primitiveStorage.save();
    }

    /**
     * @param key2
     * @param genericReturnType
     * @return
     */
    private KeyHandler<?> createKeyHandler(final String key, final Type type) {
        if (Clazz.isBoolean(type)) {
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

        } else if (type instanceof Class && ((Class<?>) type).isArray()) {

            final Class<?> ct = ((Class<?>) type).getComponentType();
            final boolean p = ct.isPrimitive();
            if (Clazz.isBoolean(ct)) {

                if (p) {
                    return new ListHandler<boolean[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultBooleanArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Boolean[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultBooleanArrayValue.class;
                        }

                    };
                }

            } else if (Clazz.isLong(ct)) {
                if (p) {
                    return new ListHandler<long[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultLongArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Long[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultLongArrayValue.class;
                        }

                    };
                }

            } else if (Clazz.isInteger(ct)) {
                if (p) {
                    return new ListHandler<int[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultIntArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Integer[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultIntArrayValue.class;
                        }

                    };
                }
            } else if (Clazz.isByte(ct)) {
                if (p) {
                    return new ListHandler<byte[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultByteArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Byte[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultByteArrayValue.class;
                        }

                    };
                }
            } else if (Clazz.isFloat(ct)) {

                if (p) {
                    return new ListHandler<float[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultFloatArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Float[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultFloatArrayValue.class;
                        }

                    };
                }
            } else if (ct == String.class) {
                return new StringListHandler(this, key, type);
            } else if (ct.isEnum()) {
                return new EnumListHandler(this, key, type);

            } else if (Clazz.isDouble(ct)) {
                if (p) {
                    return new ListHandler<double[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultDoubleArrayValue.class;
                        }

                    };
                } else {
                    return new ListHandler<Double[]>(this, key, type) {
                        @Override
                        protected Class<? extends Annotation> getDefaultAnnotation() {

                            return DefaultDoubleArrayValue.class;
                        }

                    };
                }
            } else {

                return new ObjectKeyHandler(this, key, type);
            }
        } else {

            return new ObjectKeyHandler(this, key, type);
        }
    }

    /**
     * @param e
     */
    private void error(final Throwable e) {
        new Thread("ERROR THROWER") {
            @Override
            public void run() {
                Dialog.getInstance().showExceptionDialog(e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }.start();
        // we could throw the exception here, but this would kill the whole
        // interface. So we just show a a dialog for the developer and let the
        // rest of the interface work.
    }

    protected void fireEvent(final ConfigEvent.Types type, final KeyHandler<?> keyHandler, final Object parameter) {
        this.eventSender.fireEvent(new ConfigEvent(type, keyHandler, parameter));
    }

    public Class<T> getConfigInterface() {
        return this.configInterface;
    }

    /**
     * @return
     */
    protected byte[] getCryptKey() {
        return this.key;
    }

    public ConfigEventSender<Object> getEventSender() {
        return this.eventSender;
    }

    public byte[] getKey() {
        return this.key;
    }

    /**
     * @param key2
     */
    @SuppressWarnings("unchecked")
    public KeyHandler<Object> getKeyHandler(final String key) {
        return this.getKeyHandler(key, KeyHandler.class);

    }

    /**
     * @param <RawClass>
     * @param string
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends KeyHandler<?>> E getKeyHandler(final String key, final Class<E> class1) {
        final KeyHandler<?> ret = this.keyHandlerMap.get(key.toLowerCase(Locale.ENGLISH));
        if (ret == null) { throw new NullPointerException("No KeyHandler: " + key + " in " + this.configInterface); }
        return (E) ret;
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

    public JsonKeyValueStorage getPrimitiveStorage() {
        return this.primitiveStorage;
    }

    public String getRelativCPPath() {
        return this.relativCPPath;
    }

    /**
     * @throws NullPointerException
     *             if there is no keyhandler for key
     * @param string
     * @return
     */

    public Object getValue(final String key) {

        return this.getKeyHandler(key).getValue();
    }

    private WriteStrategy writeStrategy       = null;
    private int           delayedSaveInterval = 10000;

    /**
     * The Keyvalue storage in this handler can write itself to disk based on an
     * interval.
     * 
     * <br>
     * we write to disk {@link #getDelayedSaveInterval()} MS after the last
     * change, but at least {@link #getDelayedSaveMaxInterval()} after a change
     * 
     * @param delayedSaveMaxInterval
     */
    public void setDelayedSaveMinInterval(int delayedSaveInterval) {
        this.delayedSaveInterval = delayedSaveInterval;
        updateSaveDelayer();
    }

    public WriteStrategy getWriteStrategy() {
        return writeStrategy;
    }

    public void setWriteStrategy(WriteStrategy writeStrategy) {
        this.writeStrategy = writeStrategy;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(final Object instance, final Method m, final Object[] parameter) throws Throwable {
        final long t = StorageHandler.PROFILER_MAP == null ? 0 : System.nanoTime();

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
                    ((KeyHandler<Object>) handler).setValue(parameter[0]);
                    if (writeStrategy != null) {
                        writeStrategy.write(this, handler);
                    }

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
            if (StorageHandler.PROFILER_MAP != null && m != null) {
                final long dur = System.nanoTime() - t;
                final String id = m.toString();
                Long g = StorageHandler.PROFILER_MAP.get(id);
                if (g == null) {
                    g = 0l;
                }
                StorageHandler.PROFILER_MAP.put(id, g + dur);
            }
            if (StorageHandler.PROFILER_CALLNUM_MAP != null && m != null) {
                final String id = m.toString();
                final Long g = StorageHandler.PROFILER_CALLNUM_MAP.get(id);
                StorageHandler.PROFILER_CALLNUM_MAP.put(id, g == null ? 1 : g + 1);
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
                    if (keyGetterMap.containsKey(key)) {

                        this.error(new InterfaceParseException("Key " + key + " Dupe found! " + keyGetterMap.get(key) + "<-->" + m));
                        continue;
                    }
                    keyGetterMap.put(key, m);
                    if (m.getParameterTypes().length > 0) {

                        this.error(new InterfaceParseException("Getter " + m + " has parameters."));
                        keyGetterMap.remove(key);
                        continue;
                    }
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
                        if (!found) {

                            this.error(new InterfaceParseException(e));
                            keyGetterMap.remove(key);
                            continue;
                        }
                    }
                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = this.createKeyHandler(key, m.getGenericReturnType());
                        parseMap.put(key, kh);
                    }
                    // JSonStorage.canStorePrimitive(m.getReturnType())
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m);
                    kh.setGetter(h);

                    this.methodMap.put(m, kh);
                    this.keyHandlerMap.put(key, kh);

                } else if (m.getName().startsWith("is")) {
                    key = m.getName().substring(2).toLowerCase(Locale.ENGLISH);
                    // we do not allow to setters/getters with the same name but
                    // different cases. this only confuses the user when editing
                    // the
                    // later config file
                    if (keyGetterMap.containsKey(key)) {
                        this.error(new InterfaceParseException("Key " + key + " Dupe found! " + keyGetterMap.get(key) + "<-->" + m));
                        continue;

                    }
                    keyGetterMap.put(key, m);
                    if (m.getParameterTypes().length > 0) {

                        this.error(new InterfaceParseException("Getter " + m + " has parameters."));
                        keyGetterMap.remove(key);
                        continue;
                    }
                    try {
                        JSonStorage.canStore(m.getGenericReturnType());
                    } catch (final InvalidTypeException e) {
                        this.error(new InterfaceParseException(e));
                        keyGetterMap.remove(key);
                        continue;
                    }

                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = this.createKeyHandler(key, m.getGenericReturnType());
                        parseMap.put(key, kh);
                    }
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.GETTER, key, m);
                    kh.setGetter(h);
                    this.keyHandlerMap.put(key, kh);
                    this.methodMap.put(m, kh);
                } else if (m.getName().startsWith("set")) {
                    key = m.getName().substring(3).toLowerCase(Locale.ENGLISH);
                    if (keySetterMap.containsKey(key)) {

                        this.error(new InterfaceParseException("Key " + key + " Dupe found! " + keySetterMap.get(key) + "<-->" + m));
                        continue;

                    }
                    keySetterMap.put(key, m);
                    if (m.getParameterTypes().length != 1) {
                        this.error(new InterfaceParseException("Setter " + m + " has !=1 parameters."));
                        keySetterMap.remove(key);
                        continue;

                    }
                    if (m.getReturnType() != void.class) {
                        this.error(new InterfaceParseException("Setter " + m + " has a returntype != void"));
                        keySetterMap.remove(key);
                        continue;
                    }
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
                        if (!found) {

                            this.error(new InterfaceParseException(e));
                            keySetterMap.remove(key);
                            continue;

                        }
                    }

                    KeyHandler<?> kh = parseMap.get(key);
                    if (kh == null) {
                        kh = this.createKeyHandler(key, m.getGenericParameterTypes()[0]);
                        parseMap.put(key, kh);
                    }
                    final MethodHandler h = new MethodHandler(this, MethodHandler.Type.SETTER, key, m);
                    kh.setSetter(h);
                    this.keyHandlerMap.put(key, kh);
                    this.methodMap.put(m, kh);

                } else {

                    this.error(new InterfaceParseException("Only getter and setter allowed:" + m));
                    continue;
                }
            }
            // run down the calss hirarchy to find all methods. getMethods does
            // not work, because it only finds public methods
            final Class<?>[] interfaces = clazz.getInterfaces();
            clazz = interfaces[0];

        }

        final java.util.List<Method> methodsToRemove = new ArrayList<Method>();
        for (final KeyHandler<?> kh : this.methodMap.values()) {
            try {
                kh.init();
            } catch (final Throwable e) {
                this.error(e);
                this.keyHandlerMap.remove(kh.getKey());
                if (kh.getGetter() != null) {
                    methodsToRemove.add(kh.getGetter().getMethod());

                }
                if (kh.getSetter() != null) {
                    methodsToRemove.add(kh.getSetter().getMethod());
                }

            }
        }
        for (final Method m : methodsToRemove) {
            this.methodMap.remove(m);
        }
    }

    /**
     * @param key
     * @param object
     */
    protected void putPrimitive(final String key, final Boolean value) {
        this.primitiveStorage.put(key, value);
        delayedSave();

    }

    /**
     * 
     */
    private void delayedSave() {
        if (getDelayedSaveInterval() < 0) return;
        DelayedRunnable del = delayedSaver;
        if (del != null) del.resetAndStart();
    }

    /**
     * @return
     */
    private int getDelayedSaveInterval() {
        // TODO Auto-generated method stub
        return delayedSaveInterval;
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Byte object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Double object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Enum<?> object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Float object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Integer object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();

    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final Long object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();
    }

    /**
     * @param key2
     * @param object
     */
    protected void putPrimitive(final String key2, final String object) {
        this.primitiveStorage.put(key2, object);
        delayedSave();

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

    /**
     * @param key2
     */

}
