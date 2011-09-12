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
@ApiNamespace("events")
@ApiSessionRequired
public interface EventsAPIInterface extends RemoteAPIInterface {

    @ApiDoc("establish a connection to event stream")
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response);

    @ApiDoc("establish a connection to event stream with given lastEvendID")
    public void listen(final RemoteAPIRequest request, final RemoteAPIResponse response, Long lastEventID);
}
