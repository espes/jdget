package org.appwork.storage;

import java.io.IOException;
import java.util.HashMap;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class JacksonStorageChest extends Storage {

    private HashMap<String, Object> map;
    private String name;

    @SuppressWarnings("unchecked")
    public JacksonStorageChest(String name) throws StorageException {

        map = new HashMap<String, Object>();
        this.name = name;

        try {
            HashMap<String, Object> load = ConfigInterface.getMapper().readValue(IO.readFileToString(Application.getRessource("cfg/" + name + ".json")), HashMap.class);
            map.putAll(load);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }

        // map = new JSONDeserializer<HashMap<String,
        // Object>>().deserialize(IO.readFileToString(path));

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
            } else if (def instanceof Enum) {
                put(key, (Enum) def);
            } else {
                throw new StorageException("Invalid datatype: " + def.getClass());
            }

        }

        if (def instanceof Enum && ret instanceof String) {
            try {
                ret = (E) Enum.valueOf(((Enum) def).getDeclaringClass(), (String) ret);
            } catch (Throwable e) {
                Log.exception(e);
                put(key, (Enum) def);
                ret = def;
            }
        }
        return (E) ret;

    }

    // private void initTransformer() {
    // // f transformer.put(Enum.class, new EnumTransformer());
    // }

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
            String json = ConfigInterface.getMapper().writeValueAsString(map);
            ConfigInterface.saveTo("cfg/" + name + ".json", json);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clear() throws StorageException {
        map.clear();
    }

}
