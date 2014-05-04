/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.responses
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.responses;

import java.io.IOException;
import java.io.OutputStream;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.net.HeaderCollection;

/**
 * @author daniel
 * 
 */
public interface HttpResponseInterface {
    public void closeConnection();

    public OutputStream getOutputStream(boolean sendResponseHeaders) throws IOException;

    /**
     * @return the responseCode
     */
    public ResponseCode getResponseCode();

    /**
     * @return the responseHeaders
     */
    public HeaderCollection getResponseHeaders();

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(final ResponseCode responseCode);
}
