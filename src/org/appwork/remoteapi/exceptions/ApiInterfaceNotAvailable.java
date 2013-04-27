/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.exceptions;

/**
 * @author thomas
 * 
 */
public class ApiInterfaceNotAvailable extends RemoteAPIException {

    /**
     * 
     */
    private static final long serialVersionUID = -6375479697981911029L;

    public ApiInterfaceNotAvailable() {
        super(RemoteAPIError.API_INTERFACE_NOT_FOUND);
 
    }

}
