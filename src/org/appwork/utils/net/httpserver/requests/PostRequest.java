/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.requests;

import java.io.IOException;
import java.io.InputStream;

import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class PostRequest extends HttpRequest {

    private InputStream          inputStream = null;
    private final HttpConnection connection;

    public PostRequest(final HttpConnection connection) {
        this.connection = connection;
    }

    public InputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            this.inputStream = this.connection.getInputStream();
        }
        return this.inputStream;

    }
}
