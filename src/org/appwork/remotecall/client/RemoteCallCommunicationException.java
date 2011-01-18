/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.client
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.client;

/**
 * @author thomas
 */
public class RemoteCallCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 7543663027912330426L;

    public RemoteCallCommunicationException(final Exception e) {
        super(e);
    }

    public RemoteCallCommunicationException(final String string) {
        super(string);
    }

}
