/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;

/**
 * @author daniel
 * 
 */
public abstract class RemoteAPICustomHandler extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3648527271695732720L;

    abstract public void handle(final RemoteAPIRequest request, RemoteAPIResponse response) throws IOException;
}
