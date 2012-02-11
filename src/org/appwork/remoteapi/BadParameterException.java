/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

/**
 * @author thomas
 * 
 */
public class BadParameterException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1276528269606559293L;

    public BadParameterException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BadParameterException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BadParameterException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public BadParameterException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
