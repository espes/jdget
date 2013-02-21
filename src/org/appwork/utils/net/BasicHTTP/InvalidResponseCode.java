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

import org.appwork.utils.StringUtils;
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
    public InvalidResponseCode(final HTTPConnection connection, final String message) {
        super("Invalid ResponseCode: " + connection.getResponseCode());
        this.connection = connection;
        this.message = message;

    }

    /**
     * @param connection2
     * @param str
     */
    public InvalidResponseCode(final HTTPConnection connection) {
        this(connection, null);

    }

    public HTTPConnection getConnection() {
        return connection;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid ResponseCode: " + connection.getResponseCode());

        if (StringUtils.isNotEmpty(message)) {
            sb.append("\r\nResponseBody:\r\n").append(message);
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public int getCode() {
        try {
            return connection.getResponseCode();
        } catch (final Exception e) {
        }
        return -1;
    }

}
