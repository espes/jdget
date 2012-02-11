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
public class ApiCommandNotAvailable extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6375479697981911029L;

    public ApiCommandNotAvailable() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ApiCommandNotAvailable(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ApiCommandNotAvailable(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ApiCommandNotAvailable(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
