package org.appwork.fileiconexport;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class GRPICONDIR extends Structure {
    public static class ByReference extends GRPICONDIR implements Structure.ByReference {

    }

    public static class ByValue extends GRPICONDIR implements Structure.ByValue {

    }

    // / Reserved (must be 0)
    public short idReserved;
    // / Resource type (1 for icons)
    public short idType;
    // / How many images?
    public short idCount;

    // /**
    // * The entries for each image<br>
    // * C type : GRPICONDIRENTRY[1]
    // */
    // public GRPICONDIRENTRY[] idEntries = new GRPICONDIRENTRY[1];

    public GRPICONDIR() {
        super();
        this.initFieldOrder();
    }

    public GRPICONDIR(final Pointer p) {
        super(p);

        this.initFieldOrder();
        this.read();
    }

    protected void initFieldOrder() {
        this.setFieldOrder(new java.lang.String[] { "idReserved", "idType", "idCount" });
    };
}
