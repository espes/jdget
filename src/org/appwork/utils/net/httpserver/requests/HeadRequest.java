/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.requests
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.requests;

import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class HeadRequest extends GetRequest {
    /**
     * @param connection
     */
    public HeadRequest(HttpConnection connection) {
        super(connection);       
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n----------------Request-------------------------\r\n");

        sb.append("HEAD ").append(this.getRequestedURL()).append(" HTTP/1.1\r\n");

        for (final HTTPHeader key : this.getRequestHeaders()) {

            sb.append(key.getKey());
            sb.append(": ");
            sb.append(key.getValue());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
