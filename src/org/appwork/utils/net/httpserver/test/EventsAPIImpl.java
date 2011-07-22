/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.test;

import org.appwork.remoteapi.EventsAPI;
import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class EventsAPIImpl extends EventsAPI {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.remoteapi.EventsAPI#isSessionAllowed(org.appwork.utils.net
     * .httpserver.session.HttpSession)
     */
    @Override
    public boolean isSessionAllowed(final HttpSession session) {
        if (session == null) { return false; }
        return true;
    }

}
