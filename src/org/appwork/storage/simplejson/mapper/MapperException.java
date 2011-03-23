/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

/**
 * @author thomas
 */
public class MapperException extends Exception {

    private static final long serialVersionUID = -1160630596356061500L;

    public MapperException() {
        super();
    }

    public MapperException(final String message) {
        super(message);
    }

    public MapperException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MapperException(final Throwable cause) {
        super(cause);
    }

}
