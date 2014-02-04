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
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.simplejson.JSonObject;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.LimitedInputStream;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class PostRequest extends HttpRequest {

    private static enum CONTENT_TYPE {
        X_WWW_FORM_URLENCODED,
        JSON

    }

    protected InputStream        inputStream         = null;

    protected boolean            postParameterParsed = false;
    protected List<KeyValuePair> postParameters      = null;

    /**
     * @param connection
     */
    public PostRequest(final HttpConnection connection) {
        super(connection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#
     * getParameterbyKey(java.lang.String)
     */
    @Override
    public String getParameterbyKey(final String key) throws IOException {

        List<KeyValuePair> params = getRequestedURLParameters();
        if (params != null) {
            for (final KeyValuePair param : params) {
                if (key.equalsIgnoreCase(param.key)) { return param.value; }
            }
        }

        params = getPostParameter();
        if (params != null) {
            for (final KeyValuePair param : params) {
                if (key.equalsIgnoreCase(param.key)) { return param.value; }
            }
        }

        return null;

    }

    /**
     * TODO: modify these to check if we need to wrap the inputstream again
     * 
     * @return
     * @throws IOException
     */
    public synchronized InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            final HTTPHeader transferEncoding = getRequestHeaders().get(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING);
            if (transferEncoding != null) {
                if ("chunked".equalsIgnoreCase(transferEncoding.getValue())) {
                    inputStream = new ChunkedInputStream(connection.getInputStream()) {

                        volatile boolean closed = false;

                        @Override
                        public void close() throws IOException {
                            closed = true;
                            if (PostRequest.this.connection.closableStreams()) {
                                super.close();
                            }
                        }

                        @Override
                        public int read() throws IOException {
                            if (closed) { return -1; }
                            return super.read();
                        }

                        @Override
                        public int read(final byte[] b) throws IOException {
                            if (closed) { return -1; }
                            return super.read(b);
                        }

                        @Override
                        public int read(final byte[] b, final int off, final int len) throws IOException {
                            if (closed) { return -1; }
                            return super.read(b, off, len);
                        }
                    };
                } else {
                    throw new IOException("Unknown Transfer-Encoding " + transferEncoding.getValue());
                }
            } else {
                final HTTPHeader contentLength = getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
                if (contentLength == null) { throw new IOException("No Content-Length given!"); }
                inputStream = new LimitedInputStream(connection.getInputStream(), Long.parseLong(contentLength.getValue())) {

                    volatile boolean closed = false;

                    @Override
                    public void close() throws IOException {
                        closed = true;
                        if (PostRequest.this.connection.closableStreams()) {
                            super.close();
                        }
                    }

                    @Override
                    public int read() throws IOException {
                        if (closed) { return -1; }
                        return super.read();
                    }

                    @Override
                    public int read(final byte[] b) throws IOException {
                        if (closed) { return -1; }
                        return super.read(b);
                    }

                    @Override
                    public int read(final byte[] b, final int off, final int len) throws IOException {
                        if (closed) { return -1; }
                        return super.read(b, off, len);
                    }

                };
            }
        }
        return inputStream;
    }

    /**
     * parse existing application/x-www-form-urlencoded PostParameters
     * 
     * @return
     * @throws IOException
     */
    public synchronized List<KeyValuePair> getPostParameter() throws IOException {
        if (postParameterParsed) { return postParameters; }
        final String type = getRequestHeaders().getValue(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE);
        CONTENT_TYPE content_type = null;
        if (new Regex(type, "(application/x-www-form-urlencoded)").matches()) {
            content_type = CONTENT_TYPE.X_WWW_FORM_URLENCODED;
        } else if (new Regex(type, "(application/json)").matches()) {
            content_type = CONTENT_TYPE.JSON;
        }
        JSonRequest jsonRequest = null;
        // content_type=CONTENT_TYPE.AESJSON;
        if (content_type != null) {
            String charSet = new Regex(type, "charset=(.*?)($| )").getMatch(0);
            if (charSet == null) {
                charSet = "UTF-8";
            }
            switch (content_type) {
            case JSON: {
                final byte[] jsonBytes = IO.readStream(-1, getInputStream());
                final String json = new String(jsonBytes, charSet);
                jsonRequest = JSonStorage.restoreFromString(json, new TypeRef<JSonRequest>() {
                });
            }
                break;
            case X_WWW_FORM_URLENCODED: {
                final byte[] jsonBytes = IO.readStream(-1, getInputStream());
                final String params = new String(jsonBytes, charSet);
                postParameters = HttpConnection.parseParameterList(params);
            }
                break;
            }
        }
        if (jsonRequest != null && jsonRequest.getParams() != null) {
            postParameters = new LinkedList<KeyValuePair>();
            for (final Object parameter : jsonRequest.getParams()) {
                if (parameter instanceof JSonObject) {
                    /*
                     * JSonObject has customized .toString which converts Map to
                     * Json!
                     */
                    postParameters.add(new KeyValuePair(null, parameter.toString()));
                } else {
                    final String jsonParameter = JSonStorage.serializeToJson(parameter);
                    postParameters.add(new KeyValuePair(null, jsonParameter));
                }
            }
        }
        postParameterParsed = true;
        return postParameters;
    }

    /**
     * @param params
     */
    public void setPostParameter(final List<KeyValuePair> params) {
        postParameterParsed = true;
        postParameters = params;
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();

            sb.append("\r\n----------------Request-------------------------\r\n");

            sb.append("POST ").append(getRequestedURL()).append(" HTTP/1.1\r\n");

            for (final HTTPHeader key : getRequestHeaders()) {

                sb.append(key.getKey());
                sb.append(": ");
                sb.append(key.getValue());
                sb.append("\r\n");
            }
            sb.append("\r\n");
            final List<KeyValuePair> postParams = getPostParameter();
            if (postParams != null) {
                for (final KeyValuePair s : postParams) {
                    sb.append(s.key);
                    sb.append(": ");
                    sb.append(s.value);
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
