/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage;

/**
 * @author thomas
 */
public class JSonMapperException extends RuntimeException {

    private static final long serialVersionUID = 8515960419014161385L;

    public JSonMapperException() {
        super();
    }

    public JSonMapperException(final String message) {
        super(message);
    }

    public JSonMapperException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JSonMapperException(final Throwable cause) {
        super(cause);
    }

}
