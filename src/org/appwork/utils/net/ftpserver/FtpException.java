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
public class FtpException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7426769637702941061L;
    private int code;
    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    private String message;

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public FtpException(final int code,final String message){
        this.code=code;
        this.message=message;
    }
}
