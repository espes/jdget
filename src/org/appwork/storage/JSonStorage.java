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
    private static final HashMap<String, Storage> MAP    = new HashMap<String, Storage>();
    private static File                           path;
    private static final ObjectMapper             MAPPER = new ObjectMapper(new ExtJsonFactory());
    public static final Object                    LOCK   = new Object();
    static {
        JSonStorage.MAPPER.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static public byte[]                          KEY    = new byte[] { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 };

    static {

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

    /**
     * TODO: Difference to {@link #getStorage(String)} ?
     */
    public static Storage getPlainStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            Storage ret = JSonStorage.MAP.get(name);
            if (ret == null) {
                ret = new JacksonStorageChest(name, true);
                JSonStorage.MAP.put(name, ret);
            }
            return ret;
        }
    }

    /**
     * TODO: Difference to {@link #getPlainStorage(String)} ?
     */
    public static Storage getStorage(final String name) throws StorageException {
        synchronized (JSonStorage.MAP) {
            Storage ret = JSonStorage.MAP.get(name);
            if (ret == null) {
                ret = new JacksonStorageChest(name);
                JSonStorage.MAP.put(name, ret);
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
        synchronized (JSonStorage.LOCK) {
            String stri = null;
            byte[] str = null;
            try {
                if (Application.getRessource(string + ".tmp").exists()) {
                    /* tmp files exists, try to restore */
                    Log.L.warning("TMP file " + Application.getRessource(string + ".tmp").getAbsolutePath() + " found");
                    try {
                        // load it
                        str = IO.readFile(Application.getRessource(string + ".tmp"));
                        E ret;
                        // try to parse it
                        if (new Regex(string, ".+\\.json").matches()) {
                            ret = JSonStorage.restoreFromString(stri = new String(str, "UTF-8"), type, def);
                        } else {
                            ret = JSonStorage.restoreFromString(stri = Crypto.decrypt(str, JSonStorage.KEY), type, def);
                        }

                        Log.L.warning("Could restore tmp file");
                        // replace normal file with tmp file
                        Application.getRessource(string).delete();
                        Application.getRessource(string + ".tmp").renameTo(Application.getRessource(string));
                        return ret;
                    } catch (final Exception e) {
                        Log.L.warning("Could not restore tmp file");
                        Log.exception(Level.WARNING, e);
                    } finally {
                        /* tmp file must be gone after read */
                        Application.getRessource(string + ".tmp").delete();
                    }
                }
                File res = Application.getRessource(string);
                if (!res.exists() || res.length() == 0) { return def; }
                str = IO.readFile(res);
                if (new Regex(string, ".+\\.json").matches()) {
                    return JSonStorage.restoreFromString(stri = new String(str, "UTF-8"), type, def);
                } else {
                    return JSonStorage.restoreFromString(stri = Crypto.decrypt(str, JSonStorage.KEY), type, def);
                }

            } catch (final Throwable e) {
                Log.L.severe(string + ":read:" + stri);
                try {
                    if (str != null) Log.L.severe(string + ":original:" + new String(str, "UTF-8"));
                } catch (Throwable e2) {
                    Log.exception(e2);
                }
                Log.exception(e);
            }
            return def;
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

    /**
     * @param pathname
     * @param json
     * @throws StorageException
     */
    public static void saveTo(final String pathname, final String json) throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                JSonStorage.path = Application.getRessource(pathname);
                final File tmp = new File(JSonStorage.path.getParentFile(), JSonStorage.path.getName() + ".tmp");
                tmp.getParentFile().mkdirs();
                tmp.delete();
                if (new Regex(pathname, ".+\\.json").matches()) {
                    /* uncrypted */
                    IO.writeToFile(tmp, json.getBytes("UTF-8"));
                } else {
                    /* encrypted */
                    IO.writeToFile(tmp, Crypto.encrypt(json, JSonStorage.KEY));
                }
                if (JSonStorage.path.exists()) {
                    if (!JSonStorage.path.delete()) { throw new StorageException("Could not overwrite file: " + JSonStorage.path); }
                }
                if (!tmp.renameTo(JSonStorage.path)) { throw new StorageException("Could not rename file: " + tmp + " to " + JSonStorage.path); }
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

    public static void save() {
        synchronized (JSonStorage.MAP) {
            for (final Entry<String, Storage> entry : JSonStorage.MAP.entrySet()) {
                try {
                    entry.getValue().save();
                } catch (final Throwable e) {
                    Log.exception(e);
                }
            }
        }
    }

}
