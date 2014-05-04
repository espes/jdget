/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.upload
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.upload;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.remoteapi.exceptions.APIError;

/**
 * @author Thomas
 * 
 */
public enum UploadError implements APIError {
    SIZE_MISMATCH(ResponseCode.ERROR_NOT_FOUND),
    ETAG_NOT_FOUND(ResponseCode.ERROR_NOT_FOUND),
    BAD_REQUEST(ResponseCode.ERROR_BAD_REQUEST), UPLOAD_IN_PROGRESS(ResponseCode.ERROR_FORBIDDEN), BAD_RANGE(ResponseCode.ERROR_RANGE_NOT_SUPPORTED);

    private ResponseCode code;

    private UploadError() {
        code = ResponseCode.SERVERERROR_INTERNAL;
    }

    private UploadError(final ResponseCode code) {
        this.code = code;
    }

    public ResponseCode getCode() {
        return code;
    }

}
