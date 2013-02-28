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
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class PostRequest extends HttpRequest {

    protected InputStream        inputStream         = null;
    private final HttpConnection connection;
    private boolean              postParameterParsed = false;
    private LinkedList<String[]> postParameters      = null;

    public PostRequest(final HttpConnection connection) {
        this.connection = connection;
    }

    public synchronized InputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            HTTPHeader transferEncoding = null;
            if ((transferEncoding = this.getRequestHeaders().get(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING)) != null && "chunked".equalsIgnoreCase(transferEncoding.getValue())) {
                this.inputStream = new ChunkedInputStream(this.connection.getInputStream());
            } else {
                this.inputStream = this.connection.getInputStream();
            }
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
        final String type = this.getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE);
        if (new Regex(type, "(application/x-www-form-urlencoded)").matches()) {
            String charSet = new Regex(type, "charset=(.*?)($| )").getMatch(0);
            if (charSet == null) {
                charSet = "UTF-8";
            }
            final String contentLength = this.getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
            int length = contentLength == null ? -1 : Integer.parseInt(contentLength);
            HTTPHeader chunkedTransfer = null;
            if ((chunkedTransfer = this.getRequestHeaders().get(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING)) == null || !"chunked".equalsIgnoreCase(chunkedTransfer.getValue())) {
                chunkedTransfer = null;
            }
            if (length <= 0 && chunkedTransfer == null) { throw new IOException("application/x-www-form-urlencoded without content-length"); }
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final byte[] tmp = new byte[128];
            int read = 0;
            /* TODO: check this for custom encoding */
            while ((read = this.getInputStream().read(tmp, 0, Math.min(tmp.length, length))) >= 0) {
                if (read > 0) {
                    bos.write(tmp, 0, read);
                    length -= read;
                    if (length == 0) {
                        break;
                    }
                }
            }
            final String postData = new String(bos.toByteArray(), charSet);
            this.postParameters = HttpConnection.parseParameterList(postData);
        } else if (new Regex(type, "(application/json)").matches()) {
            String charSet = new Regex(type, "charset=(.*?)($| )").getMatch(0);
            if (charSet == null) {
                charSet = "UTF-8";
            }
            /* TODO: rework */
            final String contentLength = this.getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
            final int length = contentLength == null ? -1 : Integer.parseInt(contentLength);
            final byte[] jsonBytes = IO.readStream(length, this.getInputStream());
            this.postParameters = new LinkedList<String[]>();
            this.postParameters.add(new String[] { new String(jsonBytes, charSet), null });
        }
        this.postParameterParsed = true;
        return this.postParameters;
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();

            sb.append("\r\n----------------Request-------------------------\r\n");

            sb.append("POST ").append(this.getRequestedPath()).append(" HTTP/1.1\r\n");

            for (final HTTPHeader key : this.getRequestHeaders()) {

                sb.append(key.getKey());
                sb.append(": ");
                sb.append(key.getValue());
                sb.append("\r\n");
            }
            sb.append("\r\n");
            final LinkedList<String[]> postParams = this.getPostParameter();
            if (postParams != null) {
                for (final String[] s : postParams) {
                    sb.append(s[0]);
                    sb.append(": ");
                    sb.append(s[1]);
                    sb.append("\r\n");
                }
            }
            return sb.toString();
        } catch (final Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
