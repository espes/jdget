package org.appwork.storage;

import org.appwork.utils.event.DefaultEventSender;

public abstract class Storage {

    private DefaultEventSender<StorageEvent<?>> eventSender;

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

    /**
     * @return the eventSender
     */
    public DefaultEventSender<StorageEvent<?>> getEventSender() {
        if (this.eventSender != null) { return this.eventSender; }
        synchronized (this) {
            if (this.eventSender != null) { return this.eventSender; }
            this.eventSender = new DefaultEventSender<StorageEvent<?>>();
        }
        return this.eventSender;
    }

    abstract public String getID();

    public boolean hasEventSender() {
        return this.eventSender != null;
    }

    /**
     * @param key
     * @return
     */
    abstract public boolean hasProperty(String key);

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

    abstract public int size();

}
