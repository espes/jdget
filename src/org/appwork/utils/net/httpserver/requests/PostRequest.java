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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.simplejson.JSonObject;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.net.Base64InputStream;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.LimitedInputStream;
import org.appwork.utils.net.httpserver.HttpConnection;

/**
 * @author daniel
 * 
 */
public class PostRequest extends HttpRequest {

    /**
     * @param connection
     */
    public PostRequest(HttpConnection connection) {
        super(connection);        
    }

    private static enum CONTENT_TYPE {
        X_WWW_FORM_URLENCODED,
        JSON,
        AESJSON
    }
    protected InputStream        inputStream         = null;   
    private boolean              postParameterParsed = false;
    private LinkedList<String[]> postParameters      = null;

   

    protected byte[] getAESJSON_IV(final String ID) {
        return this.connection.getAESJSon_IV(ID);
    }

    protected byte[] getAESJSON_KEY(final String ID) {
        return this.connection.getAESJSon_KEY(ID);
    }

    public synchronized InputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            final HTTPHeader transferEncoding = this.getRequestHeaders().get(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING);
            if (transferEncoding != null) {
                if ("chunked".equalsIgnoreCase(transferEncoding.getValue())) {
                    this.inputStream = new ChunkedInputStream(this.connection.getInputStream()) {

                        volatile boolean closed = false;

                        @Override
                        public void close() throws IOException {
                            this.closed = true;
                            if (PostRequest.this.connection.closableStreams()) {
                                super.close();
                            }
                        }

                        @Override
                        public int read() throws IOException {
                            if (this.closed) { return -1; }
                            return super.read();
                        }

                        @Override
                        public int read(final byte[] b) throws IOException {
                            if (this.closed) { return -1; }
                            return super.read(b);
                        }

                        @Override
                        public int read(final byte[] b, final int off, final int len) throws IOException {
                            if (this.closed) { return -1; }
                            return super.read(b, off, len);
                        }
                    };
                } else {
                    throw new IOException("Unknown Transfer-Encoding " + transferEncoding.getValue());
                }
            } else {
                final HTTPHeader contentLength = this.getRequestHeaders().get(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH);
                if (contentLength == null) { throw new IOException("No Content-Length given!"); }
                this.inputStream = new LimitedInputStream(this.connection.getInputStream(), Long.parseLong(contentLength.getValue())) {

                    volatile boolean closed = false;

                    @Override
                    public void close() throws IOException {
                        this.closed = true;
                        if (PostRequest.this.connection.closableStreams()) {
                            super.close();
                        }
                    }

                    @Override
                    public int read() throws IOException {
                        if (this.closed) { return -1; }
                        return super.read();
                    }

                    @Override
                    public int read(final byte[] b) throws IOException {
                        if (this.closed) { return -1; }
                        return super.read(b);
                    }

                    @Override
                    public int read(final byte[] b, final int off, final int len) throws IOException {
                        if (this.closed) { return -1; }
                        return super.read(b, off, len);
                    }

                };
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
        CONTENT_TYPE content_type = null;
        if (new Regex(type, "(application/x-www-form-urlencoded)").matches()) {
            content_type = CONTENT_TYPE.X_WWW_FORM_URLENCODED;
        } else if (new Regex(type, "(application/json)").matches()) {
            content_type = CONTENT_TYPE.JSON;
        } else if (new Regex(type, "(application/aesjson-)").matches()) {
            content_type = CONTENT_TYPE.AESJSON;
        }
        JSonRequest jsonRequest = null;
        if (content_type != null) {
            String charSet = new Regex(type, "charset=(.*?)($| )").getMatch(0);
            if (charSet == null) {
                charSet = "UTF-8";
            }
            switch (content_type) {
            case JSON: {
                final byte[] jsonBytes = IO.readStream(-1, this.getInputStream());
                final String json = new String(jsonBytes, charSet);
                jsonRequest = JSonStorage.restoreFromString(json, new TypeRef<JSonRequest>() {
                });
            }
                break;
            case X_WWW_FORM_URLENCODED: {
                final byte[] jsonBytes = IO.readStream(-1, this.getInputStream());
                final String params = new String(jsonBytes, charSet);
                this.postParameters = HttpConnection.parseParameterList(params);
            }
                break;
            case AESJSON: {
                try {
                    final String ID = new Regex(type, "application/aesjson-([a-zA-Z0-9]+)").getMatch(0);
                    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    final byte[] IV = this.getAESJSON_IV(ID);
                    if (IV == null) { throw new IOException("Invalid AESJSON Request"); }
                    final IvParameterSpec ivSpec = new IvParameterSpec(IV);
                    final byte[] KEY = this.getAESJSON_KEY(ID);
                    if (KEY == null) { throw new IOException("Invalid AESJSON Request"); }
                    final SecretKeySpec skeySpec = new SecretKeySpec(KEY, "AES");
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                    final byte[] jsonBytes = IO.readStream(-1, new CipherInputStream(new Base64InputStream(this.getInputStream()), cipher));
                    final String json = new String(jsonBytes, charSet);
                    jsonRequest = JSonStorage.restoreFromString(json, new TypeRef<JSonRequest>() {
                    });
                    if (!this.connection.isJSonRequestValid(jsonRequest)) {
                        //
                        throw new IOException("Invalid AESJSON Request");
                    }
                } catch (final NoSuchPaddingException e) {
                    throw new IOException(e);
                } catch (final NoSuchAlgorithmException e) {
                    throw new IOException(e);
                } catch (final InvalidKeyException e) {
                    throw new IOException(e);
                } catch (final InvalidAlgorithmParameterException e) {
                    throw new IOException(e);
                }
            }
                break;
            }
        }
        if (jsonRequest != null && jsonRequest.getParams() != null) {
            this.postParameters = new LinkedList<String[]>();
            for (final Object parameter : jsonRequest.getParams()) {
                if (parameter instanceof JSonObject) {
                    /*
                     * JSonObject has customized .toString which converts Map to
                     * Json!
                     */
                    this.postParameters.add(new String[] { parameter.toString(), null });
                } else {
                    final String jsonParameter = JSonStorage.toString(parameter);
                    this.postParameters.add(new String[] { jsonParameter, null });
                }

            }
        }
        this.postParameterParsed = true;
        return this.postParameters;
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();

            sb.append("\r\n----------------Request-------------------------\r\n");

            sb.append("POST ").append(this.getRequestedURL()).append(" HTTP/1.1\r\n");

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
