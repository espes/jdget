package org.appwork.remotecall.client;

import org.appwork.storage.Storable;

public class SerialiseException extends Exception implements Storable {

    private static final long serialVersionUID = 1514282378635362226L;

    @SuppressWarnings("unused")
    private SerialiseException() {
        // we need this for json serial
    }

    public SerialiseException(final Exception e) {
        super(e);
    }

}
