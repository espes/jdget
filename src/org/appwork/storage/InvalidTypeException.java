/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage;

/**
 * @author thomas
 * 
 */
public class InvalidTypeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public InvalidTypeException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public InvalidTypeException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidTypeException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public InvalidTypeException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
