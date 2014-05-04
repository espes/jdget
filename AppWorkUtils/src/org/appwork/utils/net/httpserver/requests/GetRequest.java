/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.requests
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.requests;

import java.io.IOException;
import java.util.List;

import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class GetRequest extends HttpRequest {

    /**
     * @param connection
     */
    public GetRequest(final HttpConnection connection) {
        super(connection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#
     * getParameterbyKey(java.lang.String)
     */
    @Override
    public String getParameterbyKey(final String key) throws IOException {
        final List<KeyValuePair> params = this.getRequestedURLParameters();
        if (params != null) {
            for (final KeyValuePair param : params) {
                if (key.equalsIgnoreCase(param.key)) { return param.value; }
            }
        }
        return null;

    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#
    // * getPostParameter()
    // */
    // @Override
    // public LinkedList<KeyValuePair> getPostParameter() throws IOException {
    // return null;
    // }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n----------------Request-------------------------\r\n");

        sb.append("GET ").append(this.getRequestedURL()).append(" HTTP/1.1\r\n");

        for (final HTTPHeader key : this.getRequestHeaders()) {

            sb.append(key.getKey());
            sb.append(": ");
            sb.append(key.getValue());
            sb.append("\r\n");
        }
        return sb.toString();
    }

}
