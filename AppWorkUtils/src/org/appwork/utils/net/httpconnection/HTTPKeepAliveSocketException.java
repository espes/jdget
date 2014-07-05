/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpconnection;

import java.io.IOException;
import java.net.Socket;

/**
 * @author daniel
 * 
 */
public class HTTPKeepAliveSocketException extends IOException {

    private Socket socket;

    public Socket getSocket() {
        return this.socket;
    }

    public HTTPKeepAliveSocketException() {
        super();
    }

    public HTTPKeepAliveSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPKeepAliveSocketException(String message) {
        super(message);
    }

    public HTTPKeepAliveSocketException(Throwable cause, final Socket socket) {
        super(cause);
        this.socket = socket;
    }

}
