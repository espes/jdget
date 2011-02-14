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
public class SMS77Message {

    public static enum TYPE {
        BASICPLUS,
        STANDARD,
        QUALITY
    }

    private String message;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    private TYPE   type   = TYPE.STANDARD;
    private String sender = null;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public SMS77Message(final String message) {
        this.message = message;
    }

    public SMS77Message(final String message, final String sender) {
        this.message = message;
        this.sender = sender;
    }

    public SMS77Message(final String message, final String sender, final TYPE type) {
        this.message = message;
        this.sender = sender;
        this.type = type;
    }

}
