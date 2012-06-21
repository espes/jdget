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
public enum NetworkStatus {

    DISCONNECT(0),
    CONNECTING(1),
    CONNECTED(2);



    private int                                             value;
    private static final HashMap<Integer, NetworkStatus> MAP = new HashMap<Integer, NetworkStatus>();
    static {
        for (NetworkStatus e : values()) {
            MAP.put(e.value, e);
        }
    }

    private NetworkStatus(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static NetworkStatus get(int val1) {
        return MAP.get(val1);
    }

}
