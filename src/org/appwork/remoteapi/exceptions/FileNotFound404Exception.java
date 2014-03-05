package org.appwork.remoteapi.exceptions;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;

public class FileNotFound404Exception extends BasicRemoteAPIException {

    public FileNotFound404Exception() {
        super("Not Found", ResponseCode.ERROR_NOT_FOUND);
    }

}
