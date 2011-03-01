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
 * @author thomas
 * 
 */
public class InterfaceParseException extends RuntimeException {

    private static final long serialVersionUID = -5793484842490407594L;

    public InterfaceParseException() {
        super();
    }

    /**
     * @param arg0
     */
    public InterfaceParseException(final String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InterfaceParseException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public InterfaceParseException(final Throwable arg0) {
        super(arg0);
    }

}
