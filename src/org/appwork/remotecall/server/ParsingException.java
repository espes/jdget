/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

/**
 * @author thomas
 * 
 */
public class ParsingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3991505578075260120L;

    /**
     * 
     */
    public ParsingException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public ParsingException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public ParsingException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public ParsingException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
