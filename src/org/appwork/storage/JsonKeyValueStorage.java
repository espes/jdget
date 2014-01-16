package org.appwork.storage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;

public class JsonKeyValueStorage extends Storage {

    private final Map<String, Object> map;
    private final String              name;
    private final File                file;
    private final boolean             plain;
    private final byte[]              key;
    private boolean                   autoPutValues = true;
    private boolean                   closed        = false;
    private final AtomicLong          setMark       = new AtomicLong(0);
    private final AtomicLong          writeMark     = new AtomicLong(0);
    private boolean                   enumCacheEnabled;

    public JsonKeyValueStorage(final File file) throws StorageException {
        this(file, false);
    }

    public JsonKeyValueStorage(final File file, final boolean plain) throws StorageException {
        this(file, plain, JSonStorage.KEY);
    }

    public JsonKeyValueStorage(final File file, final boolean plain, final byte[] key) throws StorageException {
        this.map = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
        this.plain = plain;
        this.file = file;
        this.name = file.getName();
        this.key = key;
        final HashMap<String, Object> load = JSonStorage.restoreFrom(file, plain, key, new TypeRef<HashMap<String, Object>>() {
        }, new HashMap<String, Object>());
        this.putAll(load);
    }

    /**
     * @param file
     * @param resource
     * @param b
     * @param key2
     */
    public JsonKeyValueStorage(final File file, final URL resource, final boolean plain, final byte[] key) {
        this.map = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
        this.plain = plain;
        this.file = file;
        this.name = file.getName();
        this.key = key;
        if (resource != null) {
            Log.L.info("Load JSon Storage from Classpath url: " + resource);
            try {
                final HashMap<String, Object> load = JSonStorage.restoreFromString(IO.readURL(resource), plain, key, new TypeRef<HashMap<String, Object>>() {
                }, new HashMap<String, Object>());
                this.putAll(load);
            } catch (final IOException e) {
                throw new WTFException(e);
            }
        }
        if (file.exists()) {
            Log.L.info("Prefer (merged) JSon Storage from File: " + file);
            final HashMap<String, Object> load = JSonStorage.restoreFrom(file, plain, key, new TypeRef<HashMap<String, Object>>() {
            }, new HashMap<String, Object>());
            this.putAll(load);
        }
    }

    public JsonKeyValueStorage(final String name) throws StorageException {
        this(name, false);
    }

    public JsonKeyValueStorage(final String name, final boolean plain) throws StorageException {
        this(name, plain, JSonStorage.KEY);
    }

    public JsonKeyValueStorage(final String name, final boolean plain, final byte[] key) throws StorageException {
        this.map = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
        this.name = name;
        this.plain = plain;
        this.file = Application.getResource("cfg/" + name + (plain ? ".json" : ".ejs"));
        Log.L.finer("Read Config: " + this.file.getAbsolutePath());
        this.key = key;
        final HashMap<String, Object> load = JSonStorage.restoreFrom(this.file, plain, key, new TypeRef<HashMap<String, Object>>() {
        }, new HashMap<String, Object>());
        this.putAll(load);
    }

    @Override
    public void clear() throws StorageException {
        this.map.clear();
        this.requestSave();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E get(final String key, final E def) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        Object ret = contains ? this.map.get(key) : null;

        if (ret != null && def != null && ret.getClass() != def.getClass()) {
            /* ret class different from def class, so we have to convert */
            if (def instanceof Long) {
                if (ret instanceof Integer) {
                    ret = new Long(((Integer) ret).longValue());
                } else if (ret instanceof String) {
                    ret = Long.parseLong((String) ret);
                }
            } else if (def instanceof Integer) {
                if (ret instanceof Long) {
                    ret = new Integer(((Long) ret).intValue());
                } else if (ret instanceof String) {
                    ret = Integer.parseInt((String) ret);
                }
            } else if (def instanceof Double) {
                if (ret instanceof Float) {
                    ret = ((Float) ret).doubleValue();
                }
            } else if (def instanceof Float) {
                if (ret instanceof Double) {
                    ret = ((Double) ret).floatValue();
                }
            }
        }
        // put entry if we have no entry
        if (!contains && this.autoPutValues) {
            ret = def;
            if (def instanceof Boolean) {
                this.put(key, (Boolean) def);
            } else if (def instanceof Long) {
                this.put(key, (Long) def);
            } else if (def instanceof Integer) {
                this.put(key, (Integer) def);
            } else if (def instanceof Byte) {
                this.put(key, (Byte) def);
            } else if (def instanceof String || def == null) {
                this.put(key, (String) def);
            } else if (def instanceof Enum<?>) {
                this.put(key, (Enum<?>) def);
            } else if (def instanceof Double) {
                this.put(key, (Double) def);
            } else if (def instanceof Float) {
                this.put(key, (Float) def);
            } else {
                throw new StorageException("Invalid datatype: " + (def != null ? def.getClass() : "null"));
            }
        }

        if (def instanceof Enum<?> && ret instanceof String) {
            try {
                ret = Enum.valueOf(((Enum<?>) def).getDeclaringClass(), (String) ret);
                if (this.isEnumCacheEnabled()) {
                    this.put(key, (Enum<?>) ret);
                }
            } catch (final Throwable e) {
                Log.exception(e);
                if (this.autoPutValues) {
                    this.put(key, (Enum<?>) def);
                }
                ret = def;
            }
        }
        return (E) ret;
    }

    public File getFile() {
        return this.file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.Storage#getID()
     */
    @Override
    public String getID() {
        return this.file.getAbsolutePath();
    }

    /**
     * @return the key
     */
    public byte[] getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasProperty(final String key) {
        return this.map.containsKey(key);
    }

    private Object internal_put(final String key, final Object value) {
        if (key == null) { throw new WTFException("key ==null is forbidden!"); }
        final Object ret;
        if (value != null) {
            ret = this.map.put(key, value);
        } else {
            /* not possible to save null values in concurrenthashmap */
            ret = this.map.remove(key);
        }
        this.requestSave();
        return ret;
    }

    /**
     * @return the autoPutValues
     */
    public boolean isAutoPutValues() {
        return this.autoPutValues;
    }

    /**
     * @return
     */
    private boolean isEnumCacheEnabled() {
        return this.enumCacheEnabled;
    }

    public boolean isPlain() {
        return this.plain;
    }

    public void put(final String key, final boolean value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Boolean value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Byte value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Double value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Enum<?> value) throws StorageException {
        if (this.isEnumCacheEnabled()) {
            this.internal_put(key, value);
        } else {
            if (value == null) {
                this.internal_put(key, null);
            } else {
                this.internal_put(key, value.name());
            }
        }
    }

    @Override
    public void put(final String key, final Float value) throws StorageException {
        this.internal_put(key, value);
    }

    public void put(final String key, final int value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Integer value) throws StorageException {
        this.internal_put(key, value);
    }

    public void put(final String key, final long value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final Long value) throws StorageException {
        this.internal_put(key, value);
    }

    @Override
    public void put(final String key, final String value) throws StorageException {
        this.internal_put(key, value);
    }

    private void putAll(final Map<String, Object> map) {
        if (map == null) { return; }
        final Iterator<Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, Object> next = it.next();
            if (next.getKey() == null || next.getValue() == null) {
                continue;
            }
            this.map.put(next.getKey(), next.getValue());
        }
    }

    @Override
    public Object remove(final String key) {
        if (key == null) { throw new WTFException("key ==null is forbidden!"); }
        if (this.map.containsKey(key)) {
            final Object ret = this.map.remove(key);
            this.requestSave();
            return ret;
        }
        return null;
    }

    public void requestSave() {
        this.setMark.incrementAndGet();
    }

    @Override
    public void save() throws StorageException {
        if (this.closed) { throw new StorageException("StorageChest already closed!"); }
        final long lastSetMark = this.setMark.get();
        if (this.writeMark.getAndSet(lastSetMark) != lastSetMark) {
            final String json = JSonStorage.getMapper().objectToString(this.map);
            JSonStorage.saveTo(this.file, this.plain, this.key, json);
        }
    }

    /**
     * @param autoPutValues
     *            the autoPutValues to set
     */
    public void setAutoPutValues(final boolean autoPutValues) {
        this.autoPutValues = autoPutValues;
    }

    public void setEnumCacheEnabled(final boolean enumCacheEnabled) {
        this.enumCacheEnabled = enumCacheEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.Storage#size()
     */
    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public String toString() {
        try {
            return JSonStorage.getMapper().objectToString(this.map);
        } catch (final Throwable e) {
            return this.map.toString();
        }

    }

}
