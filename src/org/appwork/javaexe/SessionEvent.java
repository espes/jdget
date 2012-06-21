/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.javaexe
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.javaexe;

import java.util.HashMap;

/**
 * @author Thomas
 * 
 */
public enum SessionEvent {

    SESSION_LOGGED(0),
    CONSOLE_CONNECT(1),
    CONSOLE_DISCONNECT(2),
    REMOTE_CONNECT(3),
    REMOTE_DISCONNECT(4),
    SESSION_LOGON(5),
    SESSION_LOGOFF(6),
    SESSION_LOCK(7),
    SESSION_UNLOCK(8),
    SESSION_REMOTE_CONTROL(9);

    private int                                         value;
    private static final HashMap<Integer, SessionEvent> MAP = new HashMap<Integer, SessionEvent>();
    static {
        for (SessionEvent e : values()) {
            MAP.put(e.value, e);
        }
    }

    private SessionEvent(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static SessionEvent get(int val1) {
        return MAP.get(val1);
    }

}
