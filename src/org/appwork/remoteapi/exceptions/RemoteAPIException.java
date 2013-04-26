/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.upload
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.exceptions;

/**
 * @author Thomas
 * 
 */
public class RemoteAPIException extends BasicRemoteAPIException {

    /**
     * @param data
     * @param sizeMismatch
     */
    public RemoteAPIException(final APIError error) {
        this(null, error, null);
    }

    public RemoteAPIException(final APIError error, final Object data) {
        this(null, error, data);
    }

    public RemoteAPIException(final Throwable e, final APIError error) {
        this(e, error, null);
    }

    public RemoteAPIException(final Throwable e, final APIError error, final Object data) {
        super(e, error.name(), error.getCode(), data);
    }

}
