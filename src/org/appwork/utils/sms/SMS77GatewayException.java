/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.sms
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.sms;

/**
 * @author daniel
 * 
 */
public class SMS77GatewayException extends Exception {

    /**
     * @param message
     * @param e
     */
    public SMS77GatewayException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * @param string
     */
    public SMS77GatewayException(String string) {
        super(string);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2599068986396222756L;

}
