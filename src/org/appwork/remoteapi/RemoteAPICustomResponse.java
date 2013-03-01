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
public abstract class RemoteAPICustomResponse<T> {

    private final T content;

    public RemoteAPICustomResponse(final T content) {
        this.content = content;
    }

    public T getResponseContent() {
        return this.content;
    }

    abstract public void sendCustomResponse(RemoteAPIRequest request, RemoteAPIResponse response, T content) throws IOException;
}
