package org.appwork.storage;

public abstract class Storage {

    public Storage() {
    }

    /**
     * Removes all saved settings in this {@link Storage}.
     * 
     * @throws StorageException
     */
    abstract public void clear() throws StorageException;

    abstract public void close();

    abstract public <E> E get(String key, E def) throws StorageException;

    public abstract byte[] getCryptKey();

    abstract public String getID();

    /**
     * @param key
     * @return
     */
    abstract public boolean hasProperty(String key);

    abstract public boolean isAutoPutValues();

    abstract public void put(String key, Boolean value) throws StorageException;

    abstract public void put(String key, Byte value) throws StorageException;

    abstract public void put(String key, Double value) throws StorageException;

    abstract public void put(String key, Enum<?> value) throws StorageException;

    abstract public void put(String key, Float value) throws StorageException;

    abstract public void put(String key, Integer value) throws StorageException;

    abstract public void put(String key, Long value) throws StorageException;

    abstract public void put(String key, String value) throws StorageException;

    /**
     * 
     * Removes The entry. This is not the same as {@code put(key,null)}
     * 
     * @param key
     * @return the removed value
     */
    public abstract Object remove(String key);

    abstract public void save() throws StorageException;

    abstract public void setAutoPutValues(boolean b);

    abstract public int size();

}
