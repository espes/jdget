/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

/**
 * @author Thomas
 * 
 */
public class IllegalTypeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1737777556287429746L;

    /**
     * 
     */
    public IllegalTypeException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public IllegalTypeException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalTypeException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public IllegalTypeException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
