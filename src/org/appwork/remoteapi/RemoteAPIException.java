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

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;

/**
 * @author daniel
 * 
 */
public class RemoteAPIException extends RuntimeException {

    private final ResponseCode code;
    private final String       message;

    public RemoteAPIException(final ResponseCode code) {
        this(code, null);
    }

    public RemoteAPIException(final ResponseCode code, final String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public ResponseCode getResponseCode() {
        return this.code;
    }
}
