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


/**
 * @author Thomas
 * 
 */
public class InternalApiException extends RemoteAPIException {

    /**
     * @param errorForbidden
     */
    public InternalApiException(final Throwable e) {
        super(e,RemoteAPIError.INTERNAL_SERVER_ERROR);
    }

    /**
     * 
     */
    public InternalApiException() {
        super(RemoteAPIError.INTERNAL_SERVER_ERROR);
    }

 
}
