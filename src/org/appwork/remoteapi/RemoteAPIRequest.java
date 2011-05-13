/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;


/**
 * @author daniel
 * 
 */
public class RemoteAPIRequest {

    private final RemoteAPIInterface iface;
    private final String             methodName;

    public RemoteAPIRequest(final RemoteAPIInterface iface, final String methodName) {
        this.iface = iface;
        this.methodName = methodName;
    }

}
