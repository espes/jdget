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
public class ApiCommandNotAvailable extends BasicRemoteAPIException {

    /**
     * 
     */
    private static final long serialVersionUID = -6375479697981911029L;

    public ApiCommandNotAvailable(final String string) {
        super(RemoteAPIError.API_COMMAND_NOT_FOUND.name(),RemoteAPIError.API_COMMAND_NOT_FOUND.getCode());
 
    }

}
