/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.handler
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.handler;

import org.appwork.utils.net.httpserver.requests.HttpRequestInterface;
import org.appwork.utils.net.httpserver.responses.HttpResponseInterface;

/**
 * @author Thomas
 * 
 */
public class RequestException extends Exception {

    /**
     * 
     */
    private static final long     serialVersionUID = 1L;
    private HttpRequestInterface  request;

    private HttpResponseInterface response;

    public RequestException() {
    }

    /**
     * @param cause
     * @param string
     */
    public RequestException(final Throwable cause, final String message) {
        super(message, cause);
    }

    public HttpRequestInterface getRequest() {
        return this.request;
    }

    public HttpResponseInterface getResponse() {
        return this.response;
    }

    /**
     * @param request
     */
    public void setRequest(final HttpRequestInterface request) {
        this.request = request;

    }

    /**
     * @param response
     */
    public void setResponse(final HttpResponseInterface response) {
        this.response = response;

    }
}
