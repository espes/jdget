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

/**
 * @author Thomas
 * 
 */
public class VolumeDevice implements DeviceType {
    public enum Type {
        MEDIA,
        NETWORK
    }

    private Type   type;
    private String volumeUnits;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getVolumeUnits() {
        return volumeUnits;
    }

    public void setVolumeUnits(String volumeUnits) {
        this.volumeUnits = volumeUnits;
    }

    private VolumeDevice() {

    }

    /**
     * @param i
     * @param j
     * @return
     */
    public static VolumeDevice create(int unitMask, int type) {
        VolumeDevice ret = new VolumeDevice();
        switch (type) {
        case 1:
            ret.type = Type.MEDIA;
            break;
        case 2:
            ret.type = Type.NETWORK;
            break;
        }
        ret.volumeUnits = getVolumeUnits(unitMask);
        return ret;
    }

    private static String getVolumeUnits(int unitMask) {
        String ret = "";

        for (int i = 0; i < 26; i++)
            if (((unitMask >> i) & 1) != 0) ret += ((char) ('A' + i)) + ": ";

        return ret.trim();
    }

}
