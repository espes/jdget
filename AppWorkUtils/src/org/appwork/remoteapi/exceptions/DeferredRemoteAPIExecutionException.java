/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.exceptions
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.exceptions;

import java.io.IOException;

import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public abstract class DeferredRemoteAPIExecutionException extends BasicRemoteAPIException {

    /**
     * @param name
     * @param code2
     */
    public DeferredRemoteAPIExecutionException() {
        super("Defered", ResponseCode.SUCCESS_OK);
    }

    public abstract void defer();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.remoteapi.exceptions.BasicRemoteAPIException#handle(org.appwork
     * .utils.net.httpserver.responses.HttpResponse)
     */
    @Override
    public boolean handle(final HttpResponse response) throws IOException {
        this.defer();
        return false;
    }
}
