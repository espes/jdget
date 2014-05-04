package org.appwork.utils.net.BasicHTTP;

import java.io.IOException;

import org.appwork.utils.net.httpconnection.HTTPConnection;


public class BasicHTTPException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final HTTPConnection connection;

    public BasicHTTPException(final HTTPConnection connection, final Exception e) {
        super(e);
        this.connection = connection;

    }

    public HTTPConnection getConnection() {
        return connection;
    }

}
