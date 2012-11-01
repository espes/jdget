/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.session;

import org.appwork.remoteapi.ApiDoc;
import org.appwork.remoteapi.ApiNamespace;
import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;

/**
 * @author daniel
 * 
 */
@ApiNamespace("session")
public interface LoginAPIInterface extends RemoteAPIInterface {

    @ApiDoc("invalides the current token")
    public boolean disconnect(final RemoteAPIRequest request);

    @ApiDoc("returns an un/authenticated token for given username and password or \"error\" in case login failed")
    public String handshake(final RemoteAPIRequest request, String user, String password);
}
