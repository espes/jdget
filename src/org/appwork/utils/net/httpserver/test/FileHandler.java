/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.net.ChunkedOutputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.Input2OutputStreamForwarder;
import org.appwork.utils.net.httpserver.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class FileHandler implements HttpRequestHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpRequestHandler#canHandle(org.appwork
     * .utils.net.httpserver.requests.HttpRequest)
     */

    public boolean canHandle(final HttpRequest request) {
        final String path = request.getRequestedPath();
        final File f = new File("/home/daniel", path);
        if (f.exists() && f.isFile()) { return true; }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpRequestHandler#onGetRequest(org.
     * appwork.utils.net.httpserver.requests.GetRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onGetRequest(final GetRequest request, final HttpResponse response) {
        if (!this.canHandle(request)) { return false; }
        final String path = request.getRequestedPath();
        final File f = new File("/home/daniel", path);
        response.setResponseCode(ResponseCode.SUCCESS_OK);
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/octet-stream"));
        try {
            response.getResponseHeaders().add(new HTTPHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(f.getName(), "UTF-8")));
        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
        try {
            final ChunkedOutputStream co = new ChunkedOutputStream(response.getOutputStream(), 2048);
            final Input2OutputStreamForwarder fw = new Input2OutputStreamForwarder(new FileInputStream(f), co, 4096);
            try {
                fw.forward();
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            co.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpRequestHandler#onPostRequest(org
     * .appwork.utils.net.httpserver.requests.PostRequest,
     * org.appwork.utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean onPostRequest(final PostRequest request, final HttpResponse response) {
        return false;
    }

}
