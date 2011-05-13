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

import java.util.LinkedList;

import org.appwork.utils.net.HeaderCollection;

/**
 * @author daniel
 * 
 */
public abstract class HttpRequest {

    protected Object               handlerExtension       = null;

    protected String               requestedURL           = null;

    protected HeaderCollection     requestHeaders         = null;

    protected String               requestedPath          = null;

    protected LinkedList<String[]> requestedURLParameters = null;

    /**
     * @return the handlerExtension
     */
    public Object getHandlerExtension() {
        return this.handlerExtension;
    }

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
     * @param handlerExtension
     *            the handlerExtension to set
     */
    public void setHandlerExtension(final Object handlerExtension) {
        this.handlerExtension = handlerExtension;
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
}
