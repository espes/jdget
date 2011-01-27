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

import java.io.IOException;

/**
 * @author daniel
 * 
 */
public class FtpFileNotExistException extends FtpException {

    /**
     * @param code
     * @param message
     */
    public FtpFileNotExistException() {
        super(550, "No such file or directory.");
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5751161549164357818L;

}
