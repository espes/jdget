/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.BasicHTTP
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.BasicHTTP;

import java.io.IOException;

import org.appwork.utils.Exceptions;
import org.appwork.utils.net.httpconnection.HTTPConnection;

/**
 * @author Thomas
 * 
 */
public class InvalidResponseCode extends IOException {

    final private HTTPConnection connection;
    private String               message;

    /**
     * @param connection
     */
    public InvalidResponseCode(final HTTPConnection connection, String message) {
        super("Invalid ResponseCode: " + connection.getResponseCode());
        this.connection = connection;
        this.message = message;

    }

    /**
     * @param connection2
     * @param str
     */
    public InvalidResponseCode(HTTPConnection connection) {
        this(connection, null);

    }

    public HTTPConnection getConnection() {
        return this.connection;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid ResponseCode: " + this.connection.getResponseCode());
        try {
            if (this.connection != null) {
                sb.append(this.connection);
            }
        } catch (final Throwable e) {
            sb.append(Exceptions.getStackTrace(e));
        }
        if (message != null) {
            sb.append("\r\nResponseBody:\r\n").append(message);
        }
        return sb.toString();
    }

}
