/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

import org.appwork.storage.Storable;
import org.appwork.storage.simplejson.Ignores;

/**
 * @author thomas
 * 
 */

@Ignores({ "public java.lang.String java.lang.Throwable.getLocalizedMessage()", "public synchronized java.lang.Throwable java.lang.Throwable.getCause()", "public final native java.lang.Class java.lang.Object.getClass()", "native int java.lang.Throwable.getStackTraceDepth()", "private synchronized java.lang.StackTraceElement[] java.lang.Throwable.getOurStackTrace()", "public java.lang.StackTraceElement[] java.lang.Throwable.getStackTrace()", "public final synchronized java.lang.Throwable[] java.lang.Throwable.getSuppressed()"

})
public class RemoteCallException extends Exception implements Storable {
    /**
     * 
     */
    private static final long serialVersionUID = -2759172060515469671L;

    public RemoteCallException() {
        super();

        // required for Storable;

    }

    public RemoteCallException(String string) {
        this();
        setMessage(string);

    }

    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }

}
