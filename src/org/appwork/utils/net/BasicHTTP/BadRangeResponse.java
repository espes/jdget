/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.BasicHTTP
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.BasicHTTP;

import java.io.IOException;

import org.appwork.utils.net.httpconnection.HTTPConnection;

/**
 * @author Thomas
 * 
 */
public class BadRangeResponse extends IOException {

    private HTTPConnection connection;

    public HTTPConnection getConnection() {
        return connection;
    }

    /**
     * @param connection
     */
    public BadRangeResponse(final HTTPConnection connection) {
        super("Got Non Range Response for a Range Request");
        this.connection = connection;
    }

}
