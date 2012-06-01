package org.jdownloader.api;

import org.appwork.utils.net.httpserver.session.HttpSession;
import org.appwork.utils.net.httpserver.session.HttpSessionController;
import org.jdownloader.controlling.UniqueSessionID;

public class RemoteAPISession implements HttpSession {

    private boolean                                      alive      = true;
    private String                                       sessionID  = null;
    private HttpSessionController<? extends HttpSession> controller = null;

    protected RemoteAPISession(HttpSessionController<? extends HttpSession> controller) {
        this.sessionID = "" + new UniqueSessionID() + ("_" + System.currentTimeMillis()).hashCode() + System.currentTimeMillis();
        this.controller = controller;
    }

    public HttpSessionController<? extends HttpSession> getSessionController() {
        return controller;
    }

    public String getSessionID() {
        return sessionID;
    }

    public boolean isAlive() {
        return alive;
    }

    protected void setAlive(boolean b) {
        this.alive = b;
    }

}
