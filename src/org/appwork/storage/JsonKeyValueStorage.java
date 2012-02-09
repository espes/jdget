package org.appwork.storage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;

public class JsonKeyValueStorage extends Storage {

    private final HashMap<String, Object> map;
    private final String                  name;
    private final File                    file;
    private final boolean                 plain;
    private final byte[]                  key;
    private boolean                       autoPutValues = true;
    private boolean                       closed        = false;
    private boolean                       changed       = false;

    public JsonKeyValueStorage(final File file) throws StorageException {
        this(file, false);
    }

    public JsonKeyValueStorage(final File file, final boolean plain) throws StorageException {
        this(file, plain, JSonStorage.KEY);
    }

    public JsonKeyValueStorage(final File file, final boolean plain, final byte[] key) throws StorageException {
        this.map = new HashMap<String, Object>();
        this.plain = plain;
        this.file = file;
        this.name = file.getName();
        this.key = key;
        final HashMap<String, Object> load = JSonStorage.restoreFrom(file, plain, key, new TypeRef<HashMap<String, Object>>() {
        }, new HashMap<String, Object>());

        this.map.putAll(load);
    }

    public JsonKeyValueStorage(final String name) throws StorageException {
        this(name, false);
    }

    public JsonKeyValueStorage(final String name, final boolean plain) throws StorageException {
        this(name, plain, JSonStorage.KEY);
    }

    public JsonKeyValueStorage(final String name, final boolean plain, final byte[] key) throws StorageException {
        this.map = new HashMap<String, Object>();
        this.name = name;
        this.plain = plain;
        this.file = Application.getResource("cfg/" + name + (plain ? ".json" : ".ejs"));
        Log.L.finer("Read Config: " + this.file.getAbsolutePath());

        this.key = key;

        final HashMap<String, Object> load = JSonStorage.restoreFrom(this.file, plain, key, new TypeRef<HashMap<String, Object>>() {
        }, new HashMap<String, Object>());
        // Log.L.finer(JSonStorage.toString(load));
        this.map.putAll(load);
    }

    /**
     * @param file
     * @param resource
     * @param b
     * @param key2
     */
    public JsonKeyValueStorage(File file, URL resource, boolean plain, byte[] key) {
        this.map = new HashMap<String, Object>();
        this.plain = plain;
        this.file = file;
        this.name = file.getName();
        this.key = key;
      
        if (resource != null) {

            try {
                final HashMap<String, Object> load = JSonStorage.restoreFromString(IO.readURL(resource), plain, key, new TypeRef<HashMap<String, Object>>() {
                }, new HashMap<String, Object>());
                this.map.putAll(load);
            } catch (IOException e) {
                throw new WTFException(e);
            }
        }
        if (file.exists()) {
            final HashMap<String, Object> load = JSonStorage.restoreFrom(file, plain, key, new TypeRef<HashMap<String, Object>>() {
            }, new HashMap<String, Object>());

            this.map.putAll(load);
        }
    }

    @Override
    public void clear() throws StorageException {
        Entry<String, Object> next;
        for (final Iterator<Entry<String, Object>> it = this.map.entrySet().iterator(); it.hasNext();) {
            next = it.next();
            it.remove();
            if (this.hasEventSender()) {
                this.getEventSender().fireEvent(new StorageKeyRemovedEvent<Object>(this, next.getKey(), next.getValue()));
            }
        }
        this.map.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.Storage#close()
     */
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
                    // this is normal, because jackson converts tiny longs to
                    // ints automatically
                    // Log.exception(Level.FINE, new
                    // Exception("Had to convert integer to long for storage " +
                    // this.name + "." + key + "=" + ret));
                    ret = new Long(((Integer) ret).longValue());
                }
            } else if (def instanceof Integer) {
                if (ret instanceof Long) {
                    // Log.exception(Level.FINE, new
                    // Exception("Had to convert long to integer for storage " +
                    // this.name + "." + key + "=" + ret));
                    ret = new Integer(((Long) ret).intValue());
                }
            } else

            if (def instanceof Double) {
                if (ret instanceof Float) {
                    // this is normal, because jackson converts tiny longs to
                    // ints automatically
                    // Log.exception(Level.FINE, new
                    // Exception("Had to convert integer to long for storage " +
                    // this.name + "." + key + "=" + ret));
                    ret = ((Float) ret).doubleValue();
                }
            } else if (def instanceof Float) {
                if (ret instanceof Double) {
                    // Log.exception(Level.FINE, new
                    // Exception("Had to convert long to integer for storage " +
                    // this.name + "." + key + "=" + ret));
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
            } catch (final Throwable e) {
                this.map.remove(key);
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

    /* WARNING: you should know what you are doing! */
    public HashMap<String, Object> getInternalStorageMap() {
        return this.map;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.Storage#hasProperty(java.lang.String)
     */
    @Override
    public boolean hasProperty(final String key) {
        // TODO Auto-generated method stub
        return this.map.containsKey(key);
    }

    /**
     * @return the autoPutValues
     */
    public boolean isAutoPutValues() {
        return this.autoPutValues;
    }

    public boolean isPlain() {
        return this.plain;
    }

    public void put(final String key, final boolean value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final boolean old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Boolean>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Boolean>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Boolean value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Boolean old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Boolean>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Boolean>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Byte value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Byte old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Byte>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Byte>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Double value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Double old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Double>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Double>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Enum<?> value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Enum<?> old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Enum<?>>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Enum<?>>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Float value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Float old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Float>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Float>(this, key, value));
        }
    }

    public void put(final String key, final int value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Integer old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Integer>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Integer>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Integer value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Integer old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Integer>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Integer>(this, key, value));
        }
    }

    public void put(final String key, final long value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Long old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Long>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Long>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final Long value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final Long old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<Long>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<Long>(this, key, value));
        }
    }

    @Override
    public void put(final String key, final String value) throws StorageException {
        final boolean contains = this.map.containsKey(key);
        final String old = contains ? this.get(key, value) : null;
        this.map.put(key, value);
        this.changed = true;
        if (!this.hasEventSender()) { return; }
        if (contains) {
            this.getEventSender().fireEvent(new StorageValueChangeEvent<String>(this, key, old, value));
        } else {
            this.getEventSender().fireEvent(new StorageKeyAddedEvent<String>(this, key, value));
        }
    }

    @Override
    public Object remove(final String key) {
        Object ret;
        if ((ret = this.map.remove(key)) != null) {
            this.changed = true;
            if (this.hasEventSender()) {
                this.getEventSender().fireEvent(new StorageKeyRemovedEvent<Object>(this, key, ret));
            }
        }
        return ret;
    }

    @Override
    public void save() throws StorageException {
        if (this.closed) { throw new StorageException("StorageChest already closed!"); }
        if (this.changed == false) { return; }
        synchronized (JSonStorage.LOCK) {
            /*
             * writer are not threadsafe,
             * http://wiki.fasterxml.com/JacksonBestPracticeThreadSafety
             */
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
            /* this is not ThreadSafe!! */
            return JSonStorage.getMapper().objectToString(this.map);
        } catch (final Throwable e) {
            return this.map.toString();
        }

    }

}
