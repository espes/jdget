/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

/**
 * @author daniel
 *
 */
public class FtpNotLoginException extends FtpException {

    /**
     * 
     */
    private static final long serialVersionUID = 3733057587016114878L;

    /**
     * @param code
     * @param message
     */
    public FtpNotLoginException() {
        super(530, "Not logged in");
    }

}
