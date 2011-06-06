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

import java.lang.reflect.Type;

/**
 * @author thomas
 * 
 */
public class InvalidTypeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Type              type;

    /**
     * 
     */
    public InvalidTypeException() {
        super();
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

    /**
     * @param gType
     * @param message
     */
    public InvalidTypeException(final Type gType, final String message) {
        super(message);
        this.type = gType;
        // TODO Auto-generated constructor stub
    }

    public Type getType() {
        return this.type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

}
