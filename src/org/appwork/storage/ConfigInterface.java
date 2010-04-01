package org.appwork.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class ConfigInterface {
    private static final HashMap<String, Storage> MAP = new HashMap<String, Storage>();
    private static File path;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * @return the mapper Mapper is Thread safe according to
     *         http://wiki.fasterxml
     *         .com/JacksonBestPracticeThreadSafety?highlight
     *         =%28\bCategoryJackson\b%29
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    static {

        // JsonGenerator.useDefaultPrettyPrinter();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Iterator<Entry<String, Storage>> it = MAP.entrySet().iterator(); it.hasNext();) {
                    try {
                        it.next().getValue().save();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Log.exception(e);
                    }
                }

            }
        });
    }

    public synchronized static Storage getStorage(String name) throws StorageException {
        Storage ret = MAP.get(name);
        if (ret == null) {
            ret = new JacksonStorageChest(name);
            MAP.put(name, ret);
        }
        return ret;
    }

    /**
     * @param path2
     * @param tmp2
     * @param json
     */
    public static void saveTo(String pathname, String json) throws StorageException {
        try {
            path = Application.getRessource(pathname);
            File tmp = new File(path.getParentFile(), path.getName() + ".tmp");
            tmp.getParentFile().mkdirs();
            tmp.delete();
            IO.writeStringToFile(tmp, json);
            if (path.exists()) {
                if (!path.delete()) { throw new StorageException("Could not overwrite file: " + path); }
            }
            if (!tmp.renameTo(path)) { throw new StorageException("Could not rename file: " + tmp + " to " + path); }
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * @param string
     * @param list
     */
    public static void storeTo(String string, Object list) {
        try {
            saveTo(string, MAPPER.writeValueAsString(list));
        } catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (StorageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    @SuppressWarnings("unchecked")
    public static <E> E restoreFrom(String string, TypeReference<E> type, E def) {
        try {
            if (!Application.getRessource(string).exists()) return def;
            if (type != null) {
                return (E) MAPPER.readValue(Application.getRessource(string), type);
            } else {
                return (E) MAPPER.readValue(Application.getRessource(string), def.getClass());
            }

        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return def;

    }

}
