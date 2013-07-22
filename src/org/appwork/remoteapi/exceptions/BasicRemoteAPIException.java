/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.exceptions;

import java.io.IOException;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpserver.handler.RequestException;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author Thomas
 * 
 */
public class BasicRemoteAPIException extends RequestException {

    private final String       type;
    private final ResponseCode code;
    private final Object       data;

    /**
     * @param name
     * @param code2
     */
    public BasicRemoteAPIException(final String name, final ResponseCode code2) {
        this(null, name, code2, null);
    }

    /**
     * @param cause
     * @param name
     * @param code
     * @param data
     */
    public BasicRemoteAPIException(final Throwable cause, final String name, final ResponseCode code, final Object data) {
        super(cause, name + "(" + code + ")");
        this.data = data;
        this.type = name;
        this.code = code;
    }

    public ResponseCode getCode() {
        return this.code;
    }

    public Object getData() {
        return this.data;
    }

    public String getType() {
        return this.type;
    }

    /**
     * @param response
     * @throws IOException
     */
    public boolean handle(final HttpResponse response) throws IOException {

        byte[] bytes;
        final String str = JSonStorage.serializeToJson(new DeviceErrorResponse(this.getType(), this.data));
        bytes = str.getBytes("UTF-8");
        response.setResponseCode(this.getCode());
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text; charset=UTF-8"));
        response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
        response.getOutputStream(true).write(bytes);
        response.getOutputStream(true).flush();
        return true;

    }

}
