/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.BasicHTTP
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.BasicHTTP;

import java.io.IOException;

/**
 * @author Thomas
 *
 */
public class WriteIOException extends IOException {

    /**
     * 
     */
    public WriteIOException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WriteIOException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WriteIOException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WriteIOException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
