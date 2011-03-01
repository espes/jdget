/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author thomas
 * 
 */
public class HTTPException extends IOException {

    private static final long serialVersionUID = -4661795439663319073L;
    private HttpURLConnection connection;

    public HTTPException() {
        super();
    }

    /**
     * @param connection
     * @param string
     * @param e
     */
    public HTTPException(final HttpURLConnection connection, final String string, final IOException e) {
        super(string, e);
        this.connection = connection;

    }

    public HTTPException(final String arg0) {
        super(arg0);
    }

    public HTTPException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    public HTTPException(final Throwable arg0) {
        super(arg0);
    }

    public HttpURLConnection getConnection() {
        return this.connection;
    }

}
