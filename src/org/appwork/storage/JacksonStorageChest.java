package org.appwork.storage;

import java.io.IOException;
import java.util.HashMap;

import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

public class JacksonStorageChest extends Storage {

    private final HashMap<String, Object> map;
    private final String name;
    private String filename = null;
    private boolean plain;

    public JacksonStorageChest(String name) throws StorageException {
        this(name, false);
    }

    /**
     * @param name2
     * @param b
     */

    public JacksonStorageChest(String name, boolean plain) {
        this.map = new HashMap<String, Object>();
        this.name = name;
        this.plain = plain;
        this.filename = "cfg/" + name + (plain ? ".json" : ".ejs");
        synchronized (JSonStorage.LOCK) {
            HashMap<String, Object> load = JSonStorage.restoreFrom(filename, null, new HashMap<String, Object>());
            map.putAll(load);
        }
    }

    public boolean isPlain() {
        return plain;
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E get(String key, E def) throws StorageException {
        Object ret = map.get(key);
        if (ret == null && def != null) {
            ret = def;
            if (def instanceof Boolean) {
                put(key, (Boolean) def);
            } else if (def instanceof Long) {
                put(key, (Long) def);
            } else if (def instanceof Integer) {
                put(key, (Integer) def);
            } else if (def instanceof Byte) {
                put(key, (Byte) def);
            } else if (def instanceof String) {
                put(key, (String) def);
            } else if (def instanceof Enum<?>) {
                put(key, (Enum<?>) def);
            } else if (def instanceof Double) {
                put(key, (Double) def);
            } else {
                throw new StorageException("Invalid datatype: " + def.getClass());
            }
        }

        if (def instanceof Enum<?> && ret instanceof String) {
            try {
                ret = Enum.valueOf(((Enum<?>) def).getDeclaringClass(), (String) ret);
            } catch (Throwable e) {
                Log.exception(e);
                put(key, (Enum<?>) def);
                ret = def;
            }
        }
        return (E) ret;

    }

    @Override
    public void put(String key, Boolean value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, Byte value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, Enum<?> value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, Integer value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, Long value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, String value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void save() throws StorageException {
        synchronized (JSonStorage.LOCK) {
            try {
                String json = null;
                /*
                 * writer are not threadsafe,
                 * http://wiki.fasterxml.com/JacksonBestPracticeThreadSafety
                 */
                json = JSonStorage.getMapper().writeValueAsString(map);
                JSonStorage.saveTo(filename, json);
            } catch (JsonGenerationException e) {
                Log.exception(e);
            } catch (JsonMappingException e) {
                Log.exception(e);
            } catch (IOException e) {
                Log.exception(e);
            }
        }
    }

    @Override
    public void clear() throws StorageException {
        map.clear();
    }

    @Override
    public void put(String key, Double value) throws StorageException {
        map.put(key, value);
    }

    @Override
    public void put(String key, Float value) throws StorageException {
        map.put(key, value);
    }

}
