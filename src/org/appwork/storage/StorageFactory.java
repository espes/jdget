package org.appwork.storage;

public abstract class StorageFactory {

    abstract public Storage createStorage(String name) throws StorageException;
}
