package org.appwork.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.crypto.Crypto;
import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JSonStorage {
    /* hash map contains file location as string and the storage instance */
    private static final HashMap<String, Storage> MAP    = new HashMap<String, Storage>();
    private static final ObjectMapper             MAPPER = new ObjectMapper(new ExtJsonFactory());
    public static final Object                    LOCK   = new Object();
    static {
        JSonStorage.MAPPER.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }
    /* default key for encrypted json */
    static public byte[]                          KEY    = new byte[] { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 };

    static {
        /* shutdown hook to save all open Storages */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                JSonStorage.save();
            }
        });
    }

    /**
     * Mapper is Thread safe according to <br>
     * http://wiki.fasterxml.com/JacksonBestPracticeThreadSafety
     * 
     * @return the mapper
     */
    public static ObjectMapper getMapper() {
        return JSonStorage.MAPPER;
    }

    public static Storage getPlainStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            final String id = Application.getRessource("cfg/" + name + ".json").getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JacksonStorageChest(name, true);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    public static Storage getPlainStorage(final File file) throws StorageException {
        synchronized (JSonStorage.MAP) {
            final String id = file.getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JacksonStorageChest(file, true);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    public static boolean addStorage(final Storage storage) {
        synchronized (JSonStorage.MAP) {
            Storage ret = JSonStorage.MAP.get(storage.getID());
            if (ret == null) {
                JSonStorage.MAP.put(storage.getID(), storage);
                return true;
            }
            return false;
        }
    }

    public static boolean removeStorage(final Storage storage) {
        synchronized (JSonStorage.MAP) {
            Storage ret = JSonStorage.MAP.remove(storage.getID());
            if (ret == null) { return false; }
            return true;
        }
    }

    public static Storage getStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            final String id = Application.getRessource("cfg/" + name + ".ejs").getAbsolutePath();
            Storage ret = JSonStorage.MAP.get(id);
            if (ret == null) {
                ret = new JacksonStorageChest(name);
                JSonStorage.MAP.put(ret.getID(), ret);
            }
            return ret;
        }
    }

    /**
     * restores a store json object
     * 
     * @param <E>
     * @param string
     *            name of the json object. example: cfg/savedobject.json
     * @param type
     *            TypeReference instance. This is important for generic classes.
     *            for example: new TypeReference<ArrayList<Contact>>(){} to
     *            restore type ArrayList<Contact>
     * @param def
     *            defaultvalue. if typeref is not set, the method tries to use
     *            the class of def as restoreclass
     * @return
     */

    public static <E> E restoreFrom(final String string, final TypeReference<E> type, final E def) {
        final boolean plain = new Regex(string, ".+\\.json").matches();
        return restoreFrom(Application.getRessource(string), plain, JSonStorage.KEY, type, def);
    }

    public static <E> E restoreFrom(final File file, boolean plain, final byte[] key, final TypeReference<E> type, final E def) {
        synchronized (JSonStorage.LOCK) {
            String stri = null;
            byte[] str = null;
            try {
                File tmpfile = new File(file.getAbsolutePath() + ".tmp");
                if (tmpfile.exists()) {
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
                Log.L.severe(file.getAbsolutePath() + ":read:" + stri);
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
     * @param string
     * @param class1
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static Object restoreFromString(final String string, final Class<?> class1) throws JsonParseException, JsonMappingException, IOException {
        synchronized (JSonStorage.LOCK) {
            return JSonStorage.MAPPER.readValue(string, class1);

        }
    }

    @SuppressWarnings("unchecked")
    public static <E> E restoreFromString(final String string, final TypeReference<E> type, final E def) throws JsonParseException, JsonMappingException, IOException {
        if (string == null) { return def; }
        synchronized (JSonStorage.LOCK) {
            if (type != null) {
                return (E) JSonStorage.MAPPER.readValue(string, type);
            } else {
                return (E) JSonStorage.MAPPER.readValue(string, def.getClass());
            }
        }
    }

    public static void save() {
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
    }

    /**
     * @param pathname
     * @param json
     * @throws StorageException
     */
    public static void saveTo(final String pathname, final String json) throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                final File file = Application.getRessource(pathname);
                final File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
                tmp.getParentFile().mkdirs();
                tmp.delete();
                if (new Regex(pathname, ".+\\.json").matches()) {
                    /* uncrypted */
                    IO.writeToFile(tmp, json.getBytes("UTF-8"));
                } else {
                    /* encrypted */
                    IO.writeToFile(tmp, Crypto.encrypt(json, JSonStorage.KEY));
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

    public static void saveTo(final File file, boolean plain, final byte[] key, final String json) throws StorageException {
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
     * @param string
     * @param list
     */
    public static void storeTo(final String string, final Object list) {
        synchronized (JSonStorage.LOCK) {
            try {
                JSonStorage.saveTo(string, JSonStorage.toString(list));
            } catch (final JsonGenerationException e) {
                Log.exception(e);
            } catch (final JsonMappingException e) {
                Log.exception(e);
            } catch (final StorageException e) {
                Log.exception(e);
            } catch (final IOException e) {
                Log.exception(e);
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
    public static String toString(final Object list) throws JsonGenerationException, JsonMappingException, IOException {
        synchronized (JSonStorage.LOCK) {
            return JSonStorage.MAPPER.writeValueAsString(list);
        }
    }

}
