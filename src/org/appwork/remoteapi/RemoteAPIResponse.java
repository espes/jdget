/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.io.OutputStream;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.responses.HttpResponse;
import org.appwork.utils.net.httpserver.responses.HttpResponseInterface;

/**
 * @author daniel
 * 
 */
public class RemoteAPIResponse implements HttpResponseInterface {

    private final HttpResponse response;

    public RemoteAPIResponse(final HttpResponse response) {
        this.response = response;
        // Remote API requests are available via CORS by default.
        this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.responses.HttpResponseInterface#
     * closeConnection()
     */
    @Override
    public void closeConnection() {
        this.response.closeConnection();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.response.getOutputStream();
    }

    public ResponseCode getResponseCode() {
        return this.response.getResponseCode();
    }

    /**
     * @return the responseHeaders
     */
    public HeaderCollection getResponseHeaders() {
        return this.response.getResponseHeaders();
    }

    @Override
    public boolean isResponseAsync() {
        return this.response.isResponseAsync();
    }

    @Override
    public void setResponseAsync(final boolean b) {
        this.response.setResponseAsync(b);

    }

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(final ResponseCode responseCode) {
        this.response.setResponseCode(responseCode);
    }

}
