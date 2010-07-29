package org.appwork.storage;

import java.io.IOException;
import java.util.HashMap;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.crypto.Crypto;
import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class JacksonStorageChest extends Storage {

    private final HashMap<String, Object> map;
    private final String name;

    @SuppressWarnings("unchecked")
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

        try {
            String str = Crypto.decrypt(IO.readFile(Application.getRessource("cfg/" + name + (plain ? ".json" : ".ejs"))), JSonStorage.KEY);
            HashMap<String, Object> load = JSonStorage.getMapper().readValue(str, HashMap.class);
            map.putAll(load);
        } catch (JsonParseException e) {
            Log.exception(e);
        } catch (JsonMappingException e) {
            Log.exception(e);
        } catch (IOException e) {
        }
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
        // can reuse, share globally
        try {
            String json = JSonStorage.getMapper().writeValueAsString(map);
            JSonStorage.saveTo("cfg/" + name + ".ejs", json);
        } catch (JsonGenerationException e) {
            Log.exception(e);
        } catch (JsonMappingException e) {
            Log.exception(e);
        } catch (IOException e) {
            Log.exception(e);
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
