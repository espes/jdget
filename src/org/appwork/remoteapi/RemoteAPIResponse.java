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
        getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
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
        response.closeConnection();
    }

    public HttpResponse getHttpResponse() {
        return response;
    }

    public OutputStream getOutputStream(final boolean sendResponseHeaders) throws IOException {
        return response.getOutputStream(sendResponseHeaders);
    }

    /**
     * @return the remoteAPI
     */
    public RemoteAPI getRemoteAPI() {
        return remoteAPI;
    }

    public ResponseCode getResponseCode() {
        return response.getResponseCode();
    }

    /**
     * @return the responseHeaders
     */
    public HeaderCollection getResponseHeaders() {
        return response.getResponseHeaders();
    }

    /**
     * @param responseCode
     *            the responseCode to set
     */
    public void setResponseCode(final ResponseCode responseCode) {
        response.setResponseCode(responseCode);
    }

    /**
     * @param gzip
     * @param b
     * @param bytes
     */

    public void sendBytes(final boolean gzip, final boolean chunked, final byte[] bytes) throws IOException {
        /* we dont want this api response to get cached */
        getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CACHE_CONTROL, "no-store, no-cache"));
        getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/json"));
        if (gzip == false) {
            getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
            getOutputStream(true).write(bytes);
        } else {
            getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_CONTENT_ENCODING, "gzip"));
            if (chunked == false) {
                final ReusableByteArrayOutputStream os = new ReusableByteArrayOutputStream(1024);
                final GZIPOutputStream out = new GZIPOutputStream(os);
                out.write(bytes);
                out.finish();
                getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, os.size() + ""));
                getOutputStream(true).write(os.getInternalBuffer(), 0, os.size());
            } else {
                getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
                ChunkedOutputStream cos = null;
                GZIPOutputStream out = null;
                try {
                    cos = new ChunkedOutputStream(getOutputStream(true));
                    out = new GZIPOutputStream(cos);
                    out.write(bytes);
                } finally {
                    try {
                        out.finish();
                    } catch (final Throwable e) {
                    }
                    try {
                        out.flush();
                    } catch (final Throwable e) {
                    }
                    try {
                        cos.sendEOF();
                    } catch (final Throwable e) {
                    }
                }
            }
        }

    }

}
