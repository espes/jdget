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

import java.util.HashMap;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.Exceptions;

/**
 * @author daniel
 * 
 */
public class RemoteAPIException extends RuntimeException {

    /**
     * 
     */
    private static final long  serialVersionUID = -7322929296778054140L;
    private final ResponseCode code;
    private final String       message;
    private final Throwable    e;

    public RemoteAPIException(final ResponseCode code) {
        this(code, null);
    }

    public RemoteAPIException(final ResponseCode code, final String message) {
        this.code = code;
        this.message = message;
        this.e = null;
    }

    public RemoteAPIException(final Throwable e) {
        super(e);
        this.e = e;
        this.message = e.getMessage();
        this.code = ResponseCode.SERVERERROR_INTERNAL;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public HashMap<String, Object> getRemoteAPIExceptionResponse() {
        if (this.e == null) { return null; }
        final HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("type", "system");
        ret.put("message", "error");
        final HashMap<String, Object> content = new HashMap<String, Object>();
        content.put("type", "exception");
        content.put("data", Exceptions.getStackTrace(this.e));
        ret.put("data", content);
        return ret;
    }

    public ResponseCode getResponseCode() {
        return this.code;
    }
}
