/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
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
public class RemoteAPI404Exception extends RemoteAPIException {

    /**
     * 
     */
    private static final long serialVersionUID = 1696075356840493501L;

    public RemoteAPI404Exception(final String filename) {
        super(ResponseCode.ERROR_NOT_FOUND, filename + " not found");
    }
}
