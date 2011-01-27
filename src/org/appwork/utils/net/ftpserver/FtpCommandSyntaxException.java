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
public class FtpCommandSyntaxException extends FtpException {

    /**
     * @param code
     * @param message
     */
    public FtpCommandSyntaxException() {
        super(501, "Syntax error in parameters or arguments");
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5220812651734358488L;

}
