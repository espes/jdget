package org.appwork.storage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.utils.Application;
import org.appwork.utils.Hash;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.crypto.Crypto;
import org.appwork.utils.logging.Log;
import org.appwork.utils.reflection.Clazz;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

public class JSonStorage {
    /* hash map contains file location as string and the storage instance */
    private static final HashMap<String, Storage> MAP         = new HashMap<String, Storage>();

    public static final Object                    LOCK        = new Object();

    private static JSONMapper                     JSON_MAPPER = new SimpleMapper();
    /* default key for encrypted json */
    static public byte[]                          KEY         = new byte[] { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 };

    static {
        /* shutdown hook to save all open Storages */
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
            @Override
            public int getHookPriority() {
                return 0;
            }

            @Override
            public void run() {
                JSonStorage.save();
            }

            @Override
            public String toString() {
                return "JSonStorage";
            }

        });

    }

    public static boolean addStorage(final Storage storage) {
        synchronized (JSonStorage.MAP) {
            final Storage ret = JSonStorage.MAP.get(storage.getID());
            if (ret == null) {
                JSonStorage.MAP.put(storage.getID(), storage);
                return true;
            }
            return false;
        }
    }

    /**
     * Cecks of the JSOn Mapper can map this Type
     * 
     * @param allowNonStorableObjects
     *            TODO
     * @param genericReturnType
     * 
     * @throws InvalidTypeException
     */
    public static void canStore(final Type gType, final boolean allowNonStorableObjects) throws InvalidTypeException {
        JSonStorage.canStoreIntern(gType, gType.toString(), allowNonStorableObjects);
    }

    /**
     * @param gType
     * @param allowNonStorableObjects
     *            TODO
     * @param string
     * @throws InvalidTypeException
     */
    private static void canStoreIntern(final Type gType, final String path, final boolean allowNonStorableObjects) throws InvalidTypeException {
        if (gType == Object.class) {
            if (allowNonStorableObjects) { return; }
            throw new InvalidTypeException(gType, "Cannot store Object: " + path);
        }
        if (gType instanceof Class) {
            final Class<?> type = (Class<?>) gType;
            if (type == void.class) { throw new InvalidTypeException(gType, "Void is not accepted: " + path); }
            if (type.isPrimitive()) { return; }
            if (type == Boolean.class || type == Long.class || type == Integer.class || type == Byte.class || type == Double.class || type == Float.class || type == String.class) { return; }
            if (type.isEnum()) { return; }

            if (type.isArray()) {
                final Class<?> arrayType = type.getComponentType();

                JSonStorage.canStoreIntern(arrayType, path + "[" + arrayType + "]", allowNonStorableObjects);
                return;
            }
            // we need an empty constructor

            if (List.class.isAssignableFrom(type)) { return;

            }
            if (Map.class.isAssignableFrom(type)) { return;

            }

            if (HashSet.class.isAssignableFrom(type)) { return;

            }
            if (Storable.class.isAssignableFrom(type) || allowNonStorableObjects) {
                try {

                    type.getDeclaredConstructor(new Class[] {});
                    for (final Method m : type.getDeclaredMethods()) {
                        if (m.getName().startsWith("get")) {

                            if (m.getParameterTypes().length > 0) { throw new InvalidTypeException(gType, "Getter " + path + "." + m + " has parameters."); }
                            JSonStorage.canStoreIntern(m.getGenericReturnType(), path + "->" + m.getGenericReturnType(), allowNonStorableObjects);

                        } else if (m.getName().startsWith("set")) {
                            if (m.getParameterTypes().length != 1) { throw new InvalidTypeException(gType, "Setter " + path + "." + m + " has != 1 Parameters."); }

                        }
                    }
                    return;
                } catch (final NoSuchMethodException e) {
                    throw new InvalidTypeException(gType, "Storable " + path + " has no empty Constructor");
                }

            }
        } else if (gType instanceof ParameterizedTypeImpl) {
            final ParameterizedTypeImpl ptype = (ParameterizedTypeImpl) gType;

            final Class<?> raw = ((ParameterizedTypeImpl) gType).getRawType();
            JSonStorage.canStoreIntern(raw, path, allowNonStorableObjects);
            for (final Type t : ptype.getActualTypeArguments()) {
                JSonStorage.canStoreIntern(t, path + "(" + t + ")", allowNonStorableObjects);
            }
            return;

        } else if (gType instanceof GenericArrayTypeImpl) {
            final GenericArrayTypeImpl atype = (GenericArrayTypeImpl) gType;
            final Type t = atype.getGenericComponentType();
            JSonStorage.canStoreIntern(t, path + "[" + t + "]", allowNonStorableObjects);
            return;
        } else {
            throw new InvalidTypeException(gType, "Generic Type Structure not implemented: " + gType.getClass() + " in " + path);
        }

        throw new InvalidTypeException(gType, "Type " + path + " is not supported.");

    }

    /**
     * @param returnType
     * @return
     */
    public static boolean canStorePrimitive(final Class<?> type) {

        return Clazz.isPrimitive(type) || type == String.class || type.isEnum();
    }

    public static JSONMapper getMapper() {
        return JSonStorage.JSON_MAPPER;
    }

    public static Storage getPlainStorage(final File file) throws StorageException {

        synchronized (JSonStorage.MAP) {
            final String id = file.getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JsonKeyValueStorage(file, true);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    /**
     * TODO: Difference to {@link #getStorage(String)} ?
     */
    public static Storage getPlainStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            final String id = Application.getResource("cfg/" + name + ".json").getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JsonKeyValueStorage(name, true);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    public static Storage getStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            final String id = Application.getResource("cfg/" + name + ".ejs").getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JsonKeyValueStorage(name);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    public static boolean removeStorage(final Storage storage) {
        synchronized (JSonStorage.MAP) {
            final Storage ret = JSonStorage.MAP.remove(storage.getID());
            if (ret == null) { return false; }
            return true;
        }
    }

    public static <E> E restoreFrom(final File file, final boolean plain, final byte[] key, final TypeRef<E> type, final E def) {
        synchronized (JSonStorage.LOCK) {
            String stri = null;
            byte[] str = null;
            try {
                final File tmpfile = new File(file.getAbsolutePath() + ".tmp");
                if (tmpfile.exists() && tmpfile.length() > 0) {
                    /* tmp files exists, try to restore */
                    Log.L.warning("TMP file " + tmpfile.getAbsolutePath() + " found");
                    try {
                        // load it
                        str = IO.readFile(tmpfile);
                        E ret;
                        // try to parse it
                        if (plain) {
                            ret = JSonStorage.restoreFromString(stri = new String(str, "UTF-8"), type, def);
                        } else {
                            ret = JSonStorage.restoreFromString(stri = Crypto.decrypt(str, key), type, def);
                        }

                        Log.L.warning("Could restore tmp file");
                        // replace normal file with tmp file
                        file.delete();
                        tmpfile.renameTo(file);
                        if (ret == null) { return def; }
                        return ret;
                    } catch (final Exception e) {
                        Log.L.warning("Could not restore tmp file");
                        Log.exception(Level.WARNING, e);
                    } finally {
                        /* tmp file must be gone after read */
                        tmpfile.delete();
                    }
                }
                final File res = file;
                if (!res.exists() || res.length() == 0) { return def; }
                str = IO.readFile(res);
                if (plain) {
                    return JSonStorage.restoreFromString(stri = new String(str, "UTF-8"), type, def);
                } else {
                    return JSonStorage.restoreFromString(stri = Crypto.decrypt(str, key), type, def);
                }

            } catch (final Throwable e) {
                Log.L.warning(file.getAbsolutePath() + ":read:" + stri);
                try {
                    if (str != null) {
                        Log.L.severe(file.getAbsolutePath() + ":original:" + new String(str, "UTF-8"));
                    }
                } catch (final Throwable e2) {
                    Log.exception(e2);
                }
                Log.exception(e);
            }
            return def;
        }
    }

    /**
     * restores a store json object
     * 
     * @param <E>
     * @param string
     *            name of the json object. example: cfg/savedobject.json
     * @param type
     *            TypeRef instance. This is important for generic classes. for
     *            example: new TypeRef<ArrayList<Contact>>(){} to restore type
     *            java.util.List<Contact>
     * @param def
     *            defaultvalue. if typeref is not set, the method tries to use
     *            the class of def as restoreclass
     * @return
     */

    public static <E> E restoreFrom(final String string, final TypeRef<E> type, final E def) {
        final boolean plain = string.toLowerCase().endsWith(".json");
        return JSonStorage.restoreFrom(Application.getResource(string), plain, JSonStorage.KEY, type, def);
    }

    public static <E> E restoreFromFile(final File file, final E def) {
        final E ret = JSonStorage.restoreFrom(file, true, null, null, def);
        if (ret == null) { return def; }
        return ret;
    }

    public static <E> E restoreFromFile(final String relPath, final E def) {
        final boolean plain = relPath.toLowerCase().endsWith(".json");
        return JSonStorage.restoreFrom(Application.getResource(relPath), plain, JSonStorage.KEY, null, def);
    }

    public static <E> E restoreFromString(final byte[] data, final boolean plain, final byte[] key, final TypeRef<E> type, final E def) {
        if (data == null) { return def; }
        String string = null;
        try {

            if (!plain) {
                string = Crypto.decrypt(data, key);
            } else {
                string = new String(data, "UTF-8");
            }
            synchronized (JSonStorage.LOCK) {
                if (type != null) {
                    return JSonStorage.JSON_MAPPER.stringToObject(string, type);
                } else {
                    return (E) JSonStorage.JSON_MAPPER.stringToObject(string, def.getClass());
                }
            }
        } catch (final Exception e) {
            Log.exception(Level.WARNING, e);
            Log.L.warning(string);
            return def;
        }
    }

    /**
     * @param <T>
     * @param string
     * @param class1
     * @throws IOException
     */
    public static <T> T restoreFromString(final String string, final Class<T> class1) throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                return JSonStorage.JSON_MAPPER.stringToObject(string, class1);
            } catch (final Exception e) {
                throw new StorageException(string, e);
            } finally {

            }
        }
    }

    public static <E> E restoreFromString(final String string, final TypeRef<E> type) {
        if (string == null || "".equals(string)) { return null; }
        synchronized (JSonStorage.LOCK) {
            return JSonStorage.JSON_MAPPER.stringToObject(string, type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> E restoreFromString(final String string, final TypeRef<E> type, final E def) {
        if (string == null || "".equals(string)) { return def; }
        try {

            synchronized (JSonStorage.LOCK) {
                if (type != null) {
                    return JSonStorage.JSON_MAPPER.stringToObject(string, type);
                } else {
                    return (E) JSonStorage.JSON_MAPPER.stringToObject(string, def.getClass());
                }
            }
        } catch (final Exception e) {
            Log.L.warning("Error parsing String: " + string);
            Log.exception(Level.WARNING, e);

            return def;
        }
    }

    public static void save() {
        Log.L.config("Start Saving Storage");
        synchronized (JSonStorage.MAP) {
            for (final Entry<String, Storage> entry : JSonStorage.MAP.entrySet()) {
                try {
                    entry.getValue().save();
                } catch (final Throwable e) {
                    Log.exception(e);
                } finally {
                    try {
                        entry.getValue().close();
                    } catch (final Throwable e2) {
                        Log.exception(e2);
                    }
                }
            }
        }
        Log.L.config("ENDED Saving Storage");
    }

    public static void saveTo(final File file, final boolean plain, final byte[] key, final String json) throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                final File tmp = new File(file.getAbsolutePath() + ".tmp");
                tmp.getParentFile().mkdirs();
                tmp.delete();
                if (plain) {
                    /* uncrypted */
                    IO.writeToFile(tmp, json.getBytes("UTF-8"));
                } else {
                    /* encrypted */
                    IO.writeToFile(tmp, Crypto.encrypt(json, key));
                }
                if (file.exists()) {
                    if (!file.delete()) { throw new StorageException("Could not overwrite file: " + file.getAbsolutePath()); }
                }
                if (!tmp.renameTo(file)) { throw new StorageException("Could not rename file: " + tmp + " to " + file); }
            } catch (final IOException e) {
                throw new StorageException(e);
            }
        }
    }

    /**
     * @param file
     * @param packageData
     */
    public static void saveTo(final File file, final Object packageData) {
        final boolean plain = file.getName().toLowerCase().endsWith(".json");
        JSonStorage.saveTo(file, plain, JSonStorage.KEY, JSonStorage.serializeToJson(packageData));
    }

    /**
     * @param pathname
     * @param json
     * @throws StorageException
     */
    public static void saveTo(final String pathname, final String json) throws StorageException {
        JSonStorage.saveTo(pathname, json, JSonStorage.KEY);
    }

    /**
     * @param pathname
     * @param json
     * @param kEY2
     */
    public static void saveTo(final String pathname, final String json, final byte[] key) {
        synchronized (JSonStorage.LOCK) {
            try {
                final File file = Application.getResource(pathname);
                final File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
                tmp.getParentFile().mkdirs();
                tmp.delete();
                if (new Regex(pathname, ".+\\.json").matches()) {
                    /* uncrypted */
                    IO.writeToFile(tmp, json.getBytes("UTF-8"));
                } else {
                    /* encrypted */
                    IO.writeToFile(tmp, Crypto.encrypt(json, key));
                }
                if (file.exists()) {
                    if (!file.delete()) { throw new StorageException("Could not overwrite file: " + file); }
                }
                if (!tmp.renameTo(file)) { throw new StorageException("Could not rename file: " + tmp + " to " + file); }
            } catch (final IOException e) {
                throw new StorageException(e);
            }
        }
    }

    /**
     * @param list
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static String serializeToJson(final Object list) throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                return JSonStorage.JSON_MAPPER.objectToString(list);
            } catch (final Exception e) {
                throw new StorageException(e);
            } finally {

            }
        }
    }

    public static void setMapper(final JSONMapper mapper) {
        JSonStorage.JSON_MAPPER = mapper;
    }

    /**
     * @param string
     * @param list
     */
    public static void storeTo(final String string, final Object list) {
        synchronized (JSonStorage.LOCK) {
            try {
                JSonStorage.saveTo(string, JSonStorage.serializeToJson(list));
            } catch (final Exception e) {
                throw new StorageException(e);
            } finally {

            }
        }
    }

    /**
     * This method throws Exceptions
     * 
     * @param string
     * @param type
     * @param def
     * @return
     */
    public static <E> E stringToObject(final String string, final TypeRef<E> type, final E def) {
        if (StringUtils.isEmpty(string)) { throw new IllegalArgumentException("cannot stringToObject from empty string"); }
        synchronized (JSonStorage.LOCK) {
            if (type != null) {
                return JSonStorage.JSON_MAPPER.stringToObject(string, type);
            } else {
                return (E) JSonStorage.JSON_MAPPER.stringToObject(string, def.getClass());
            }
        }
    }

    public static String toString(final Object list) {
        synchronized (JSonStorage.LOCK) {
            try {
                return JSonStorage.JSON_MAPPER.objectToString(list);

            } catch (final Throwable e) {
                e.printStackTrace();
            }
            return list.toString();
        }

    }


}
