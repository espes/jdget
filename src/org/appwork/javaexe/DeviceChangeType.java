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
public enum DeviceChangeType {

    QUERYCHANGECONFIG(0x0017),
    CONFIGCHANGED(0x0018),
    CONFIGCHANGECANCELED(0x0019),
    DEVICEARRIVAL(0x8000),
    DEVICEQUERYREMOVE(0x8001),
    DEVICEQUERYREMOVEFAILED(0x8002),
    DEVICEREMOVECOMPLETE(0x8004),
    DEVICEREMOVEPENDING(0x8003),
    DEVICETYPESPECIFIC(0x8005),
    CUSTOMEVENT(0x8006),
    USERDEFINED(0xFFFF);

    private int                                             value;
    private static final HashMap<Integer, DeviceChangeType> MAP = new HashMap<Integer, DeviceChangeType>();
    static {
        for (DeviceChangeType e : values()) {
            MAP.put(e.value, e);
        }
    }

    private DeviceChangeType(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static DeviceChangeType get(int val1) {
        return MAP.get(val1);
    }

}
