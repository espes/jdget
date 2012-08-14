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
import java.net.InetAddress;
import java.util.LinkedList;

import org.appwork.utils.net.HeaderCollection;

/**
 * @author daniel
 * 
 */
public abstract class HttpRequest implements HttpRequestInterface {

    protected String               requestedURL           = null;

    protected HeaderCollection     requestHeaders         = null;

    protected String               requestedPath          = null;

    protected LinkedList<String[]> requestedURLParameters = null;

    private InetAddress remoteAddress;

    public String getRequestedPath() {
        return this.requestedPath;
    }

    public String getRequestedURL() {
        return this.requestedURL;
    }



    /**
     * @return the requestedURLParameters
     */
    public LinkedList<String[]> getRequestedURLParameters() {
        return this.requestedURLParameters;
    }

    public HeaderCollection getRequestHeaders() {
        return this.requestHeaders;
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
    public void setRequestedURLParameters(final LinkedList<String[]> requestedURLParameters) {
        this.requestedURLParameters = requestedURLParameters;
    }

    public void setRequestHeaders(final HeaderCollection requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public static String getParameterbyKey(HttpRequestInterface request, String key) throws IOException {
        LinkedList<String[]> params = request.getRequestedURLParameters();
        if (params != null) {
            for (String[] param : params) {
                if (key.equalsIgnoreCase(param[0]) && param.length == 2) return param[1];
            }
        }
        params = request.getPostParameter();
        if (params != null) {
            for (String[] param : params) {
                if (key.equalsIgnoreCase(param[0]) && param.length == 2) return param[1];
            }
        }
        return null;
    }

    /**
     * @param inetAddress
     */
    public void setRemoteAddress(InetAddress remoteAddress) {
     this.remoteAddress=remoteAddress;
        
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
