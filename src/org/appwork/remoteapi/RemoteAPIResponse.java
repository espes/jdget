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
import java.util.zip.GZIPOutputStream;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.ReusableByteArrayOutputStream;
import org.appwork.utils.net.ChunkedOutputStream;
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
    private final RemoteAPI    remoteAPI;

    public RemoteAPIResponse(final HttpResponse response, final RemoteAPI remoteAPI) {
        this.response = response;
        // Remote API requests are available via CORS by default.
        this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
        this.remoteAPI = remoteAPI;
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

    public HttpResponse getHttpResponse() {
        return this.response;
    }

    public OutputStream getOutputStream(final boolean sendResponseHeaders) throws IOException {
        return this.response.getOutputStream(sendResponseHeaders);
    }

    /**
     * @return the remoteAPI
     */
    public RemoteAPI getRemoteAPI() {
        return this.remoteAPI;
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

    /**
     * @param gzip
     * @param b
     * @param bytes
     */

    public void sendBytes(final boolean gzip, final boolean chunked, final byte[] bytes) throws IOException {
        /* we dont want this api response to get cached */
        this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CACHE_CONTROL, "no-store, no-cache"));
        this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/json"));
        if (gzip == false) {
            this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
            this.getOutputStream(true).write(bytes);
        } else {
            this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
            if (chunked == false) {
                final ReusableByteArrayOutputStream os = new ReusableByteArrayOutputStream(1024);
                final GZIPOutputStream out = new GZIPOutputStream(os);
                out.write(bytes);
                out.finish();
                this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, os.size() + ""));
                this.getOutputStream(true).write(os.getInternalBuffer(), 0, os.size());
            } else {
                this.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
                ChunkedOutputStream cos = null;
                GZIPOutputStream out = null;
                cos = new ChunkedOutputStream(this.getOutputStream(true));
                out = new GZIPOutputStream(cos);
                out.write(bytes);
                out.finish();
                out.flush();
                cos.sendEOF();
            }
        }

    }

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(final ResponseCode responseCode) {
        this.response.setResponseCode(responseCode);
    }

}
