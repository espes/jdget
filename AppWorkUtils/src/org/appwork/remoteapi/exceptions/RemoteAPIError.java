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

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;

/**
 * @author Thomas
 * 
 */
public enum RemoteAPIError implements APIError {
    SESSION(ResponseCode.ERROR_FORBIDDEN),
    API_COMMAND_NOT_FOUND(ResponseCode.ERROR_NOT_FOUND),
    AUTH_FAILED(ResponseCode.ERROR_FORBIDDEN),
    FILE_NOT_FOUND(ResponseCode.ERROR_NOT_FOUND),
    INTERNAL_SERVER_ERROR(ResponseCode.SERVERERROR_INTERNAL),
    API_INTERFACE_NOT_FOUND(ResponseCode.ERROR_NOT_FOUND),
    BAD_PARAMETERS(ResponseCode.ERROR_BAD_REQUEST);

    private ResponseCode code;

    private RemoteAPIError() {
        code = ResponseCode.SERVERERROR_INTERNAL;
    }

    private RemoteAPIError(final ResponseCode code) {
        this.code = code;
    }

    public ResponseCode getCode() {
        return code;
    }
}
