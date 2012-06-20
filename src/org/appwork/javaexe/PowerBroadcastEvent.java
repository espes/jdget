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
public enum PowerBroadcastEvent {

    QUERYSUSPEND(0x0000),
    QUERYSUSPENDFAILED(0x0002),
    SUSPEND(0x0004),
    RESUMECRITICAL(0x0006),
    RESUMESUSPEND(0x0007),
    BATTERYLOW(0x0009),
    POWERSTATUSCHANGE(0x000A),
    OEMEVENT(0x000B),
    RESUMEAUTOMATIC(0x0012);
    private int                                                value;
    private static final HashMap<Integer, PowerBroadcastEvent> MAP = new HashMap<Integer, PowerBroadcastEvent>();
    static {
        for (PowerBroadcastEvent e : values()) {
            MAP.put(e.value, e);
        }
    }

    private PowerBroadcastEvent(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static PowerBroadcastEvent get(int val1) {
        return MAP.get(val1);
    }

}
