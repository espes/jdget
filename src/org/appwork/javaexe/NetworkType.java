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
public enum NetworkType {

    OTHER(1),
    ETHERNET(6),
    TOKENRING(9),
    FDDI(15),
    PPP(23),
    LOOPBACK(24),
    SLIP(28);

    private int                                        value;
    private static final HashMap<Integer, NetworkType> MAP = new HashMap<Integer, NetworkType>();
    static {
        for (NetworkType e : values()) {
            MAP.put(e.value, e);
        }
    }

    private NetworkType(int value) {
        this.value = value;

    }

    /**
     * @param val1
     * @return
     */
    public static NetworkType get(int val1) {
        return MAP.get(val1);
    }

}
