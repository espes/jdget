/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

import org.appwork.storage.Storable;

/**
 * @author thomas
 */
public class UnserialisableException extends Exception implements Storable {

    private static final long serialVersionUID = 2858489143770582621L;

    @SuppressWarnings("unused")
    private UnserialisableException() {
        // we need this for json serial
    }

    public UnserialisableException(final String stacktrace) {
        super(stacktrace);
        this.setStackTrace(new StackTraceElement[] {});
    }

    @Override
    public String getLocalizedMessage() {
        return null;
    }

}
