package org.appwork.storage;

public abstract class Storage {

    abstract public void put(String key, Boolean value) throws StorageException;

    abstract public void put(String key, Double value) throws StorageException;

    abstract public void put(String key, Float value) throws StorageException;

    abstract public void put(String key, Integer value) throws StorageException;

    abstract public void put(String key, Long value) throws StorageException;

    abstract public void put(String key, Byte value) throws StorageException;

    abstract public void put(String key, String value) throws StorageException;

    abstract public void put(String key, Enum<?> value) throws StorageException;

    abstract public void save() throws StorageException;

    abstract public <E> E get(String key, E def) throws StorageException;

    /**
     * Removes all saved settings in this {@link Storage}.
     * 
     * @throws StorageException
     */
    abstract public void clear() throws StorageException;

}
