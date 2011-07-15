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

import java.util.ArrayList;

import org.appwork.utils.net.httpserver.session.HttpSession;

/**
 * @author daniel
 * 
 */
public class EventsAPIQueue<T extends HttpSession> {

    private T                       session           = null;
    private final long              lastPushTimestamp = 0;
    private final long              lastPushID        = 0;

    private final ArrayList<Object> events            = new ArrayList<Object>();

    public EventsAPIQueue(final T session) {
        this.session = session;
    }

    /**
     * @return the session
     */
    public T getSession() {
        return this.session;
    }

}
