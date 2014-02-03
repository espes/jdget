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
import java.util.ArrayList;
import java.util.List;

import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public abstract class HttpRequest implements HttpRequestInterface {

    public static String getParameterbyKey(final HttpRequestInterface request, final String key) throws IOException {
        List<KeyValuePair> params = request.getRequestedURLParameters();
        if (params != null) {
            for (final KeyValuePair param : params) {
                if (key.equalsIgnoreCase(param.key)) { return param.value; }
            }
        }
        if (request instanceof PostRequest) {
            params = ((PostRequest) request).getPostParameter();
            if (params != null) {
                for (final KeyValuePair param : params) {
                    if (key.equalsIgnoreCase(param.key)) { return param.value; }
                }
            }
        }
        return null;
    }

    protected String               requestedURL           = null;

    protected HeaderCollection     requestHeaders         = null;

    protected String               requestedPath          = null;

    protected List<KeyValuePair>   requestedURLParameters = null;

    private List<String>           remoteAddress          = new ArrayList<String>();

    protected final HttpConnection connection;

    public HttpConnection getConnection() {
        return connection;
    }

    public HttpRequest(final HttpConnection connection) {
        this.connection = connection;
    }

    public List<String> getRemoteAddress() {
        return remoteAddress;
    }

    public String getRequestedPath() {
        return requestedPath;
    }

    public String getRequestedURL() {
        return requestedURL;
    }

    /**
     * @return the requestedURLParameters
     */
    public List<KeyValuePair> getRequestedURLParameters() {
        return requestedURLParameters;
    }

    public HeaderCollection getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * @param inetAddress
     */
    public void setRemoteAddress(final List<String> remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * @param requestedPath
     *            the requestedPath to set
     */
    public void setRequestedPath(final String requestedPath) {
        this.requestedPath = requestedPath;
    }

    /**
     * @param requestedURL
     *            the requestedURL to set
     */
    public void setRequestedURL(final String requestedURL) {
        this.requestedURL = requestedURL;
    }

    /**
     * @param requestedURLParameters
     *            the requestedURLParameters to set
     */
    public void setRequestedURLParameters(final List<KeyValuePair> requestedURLParameters) {
        this.requestedURLParameters = requestedURLParameters;
    }

    public void setRequestHeaders(final HeaderCollection requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

}
