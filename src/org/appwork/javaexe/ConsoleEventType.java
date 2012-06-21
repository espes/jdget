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
public enum ConsoleEventType {

    C_EVENT(0),
    BREAK_EVENT(1),
    CLOSE_EVENT(2),
    LOGOFF_EVENT(5),
    SHUTDOWN_EVENT(6);

    private int                                             value;
    private static final HashMap<Integer, ConsoleEventType> MAP = new HashMap<Integer, ConsoleEventType>();
    static {
        for (ConsoleEventType e : values()) {
            MAP.put(e.value, e);
        }
    }

    private ConsoleEventType(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static ConsoleEventType get(int val1) {
        return MAP.get(val1);
    }

}
