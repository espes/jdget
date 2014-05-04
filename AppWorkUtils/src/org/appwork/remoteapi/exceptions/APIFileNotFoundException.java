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
public class APIFileNotFoundException extends RemoteAPIException {

    /**
     * @param errorForbidden
     */
    public APIFileNotFoundException() {
        super(RemoteAPIError.FILE_NOT_FOUND);
    }
}
