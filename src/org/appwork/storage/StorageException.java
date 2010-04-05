package org.appwork.storage;

public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1506790924380710845L;

    public StorageException(Exception e) {
        super(e);
    }

    public StorageException(String string) {
        super(string);
    }

}
