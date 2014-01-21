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
public class BadParameterException extends RemoteAPIException {

    /**
     * 
     */
    private static final long serialVersionUID = -1276528269606559293L;

    public BadParameterException(final Throwable cause, final String msg) {
        super(cause, RemoteAPIError.BAD_PARAMETERS, msg);
    }

    /**
     * @param string
     */
    public BadParameterException(final String msg) {
        this(null, msg);
    }

}
