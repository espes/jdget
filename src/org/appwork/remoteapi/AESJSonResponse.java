/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.responses
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.net.Base64OutputStream;
import org.appwork.utils.net.ChunkedOutputStream;
import org.appwork.utils.net.HTTPHeader;

/**
 * @author daniel
 * 
 */
public class AESJSonResponse<T> extends RemoteAPICustomResponse<T> {

    protected byte[] IV;
    protected byte[] KEY;
    protected String ID;

    public AESJSonResponse(final T content, final byte[] IV, final byte[] KEY, final String ID) {
        super(content);
        this.IV = IV;
        this.KEY = KEY;
        this.ID = ID;
    }

    @Override
    public void sendCustomResponse(final RemoteAPIRequest request, final RemoteAPIResponse response, final T content) throws IOException {
        final String json = JSonStorage.toString(content);
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final IvParameterSpec ivSpec = new IvParameterSpec(this.IV);
            final SecretKeySpec skeySpec = new SecretKeySpec(this.KEY, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            /* set chunked transfer header */
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING, HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED));
            response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "application/aesjson-" + this.ID + "; charset=utf-8"));
            response.setResponseCode(ResponseCode.SUCCESS_OK);
            final CipherOutputStream os = new CipherOutputStream(new Base64OutputStream(new ChunkedOutputStream(response.getOutputStream())), cipher);
            os.write(json.getBytes("UTF-8"));
            os.close();
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException(e);
        } catch (final NoSuchPaddingException e) {
            throw new IOException(e);
        } catch (final InvalidKeyException e) {
            throw new IOException(e);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new IOException(e);
        }
    }

}
