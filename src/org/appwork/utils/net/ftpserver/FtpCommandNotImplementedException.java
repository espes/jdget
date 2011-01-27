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
public class FtpCommandNotImplementedException extends FtpException {

    /**
     * @param code
     * @param message
     */
    public FtpCommandNotImplementedException() {
        super(502, "Command not implemented");
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5128464601411561065L;

}
