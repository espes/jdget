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

import org.appwork.utils.net.httpserver.session.HttpSession;
import org.appwork.utils.net.httpserver.session.HttpSessionController;

/**
 * @author daniel
 * 
 */
public class TestSession implements HttpSession {

    private boolean alive = true;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpserver.session.HttpSession#getSessionController
     * ()
     */
    @Override
    public HttpSessionController<? extends HttpSession> getSessionController() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.session.HttpSession#getSessionID()
     */
    @Override
    public String getSessionID() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.net.httpserver.session.HttpSession#isAlive()
     */
    @Override
    public boolean isAlive() {
        return this.alive;
    }

    protected void kill() {
        this.alive = false;
    }

}
