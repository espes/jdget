package org.appwork.storage;

public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1506790924380710845L;

    public StorageException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public StorageException(final Exception e) {
        super(e);
    }

    public StorageException(final String string) {
        super(string);
    }

    public StorageException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public StorageException(final Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
