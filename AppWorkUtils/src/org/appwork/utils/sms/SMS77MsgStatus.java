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
public class SMS77MsgStatus {

    private String status;

    public String getStatus() {
        return status;
    }

    private long timestamp = -1;

    public long getTimestamp() {
        return timestamp;
    }

    protected SMS77MsgStatus(final String rets[]) {
        this.status = rets[0];
        if (rets.length > 1) this.timestamp = Long.parseLong(rets[1]);
    }

    @Override
    public String toString() {
        return status + " " + timestamp;
    }
}
