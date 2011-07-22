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

import java.util.HashMap;

import org.appwork.utils.net.httpserver.session.HttpSessionController;

/**
 * @author daniel
 * 
 */
public class HttpSessionControllerImp extends HttpSessionController<TestSession> {

    private final HashMap<String, TestSession> sessions = new HashMap<String, TestSession>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.HttpSessionController#getSession(org
     * .appwork.utils.net.httpserver.requests.HttpRequest)
     */
    @Override
    public TestSession getSession(final String id) {
        synchronized (this.sessions) {
            final TestSession ret = this.sessions.get(id);
            return ret;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.session.HttpSessionController#createSession
     * (java.lang.String, java.lang.String)
     */
    @Override
    protected TestSession newSession(final String username, final String password) {
        if (!"wron".equals(password)) {
            final TestSession session = new TestSession() {
                @Override
                public String getSessionID() {
                    return System.currentTimeMillis() + "";
                }

            };
            synchronized (this.sessions) {
                this.sessions.put(session.getSessionID(), session);
            }
            return session;
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.session.HttpSessionController#removeSession
     * (org.appwork.utils.net.httpserver.session.HttpSession)
     */
    @Override
    protected boolean removeSession(final TestSession session) {
        if (session == null) { return false; }
        synchronized (this.sessions) {
            final TestSession ret = this.sessions.remove(session.getSessionID());
            if (ret == null) { return false; }
            ret.kill();
            return true;
        }

    }

}
