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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class PostRequest extends HttpRequest {

    private InputStream          inputStream         = null;
    private final HttpConnection connection;
    private boolean              postParameterParsed = false;
    private LinkedList<String[]> postParameters      = null;

    public PostRequest(final HttpConnection connection) {
        this.connection = connection;
    }

    public synchronized InputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            this.inputStream = this.connection.getInputStream();
        }
        return this.inputStream;
    }

    /**
     * parse existing application/x-www-form-urlencoded PostParameters
     * 
     * @return
     * @throws IOException
     */
    public synchronized LinkedList<String[]> getPostParameter() throws IOException {
        if (this.postParameterParsed) { return this.postParameters; }
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(this.getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE))) {
            final String contentLength = this.getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
            int length = contentLength == null ? -1 : Integer.parseInt(contentLength);
            if (length <= 0) { throw new IOException("application/x-www-form-urlencoded without content-length"); }
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final byte[] tmp = new byte[128];
            int read = 0;
            while ((read = this.getInputStream().read(tmp, 0, Math.min(tmp.length, length))) >= 0) {
                if (read > 0) {
                    bos.write(tmp, 0, read);
                    length -= read;
                    if (length == 0) {
                        break;
                    }
                }
            }
            final String postData = new String(bos.toByteArray(), "UTF-8");
            this.postParameters = HttpConnection.parseParameterList(postData);
        }
        this.postParameterParsed = true;
        return this.postParameters;
    }
}
