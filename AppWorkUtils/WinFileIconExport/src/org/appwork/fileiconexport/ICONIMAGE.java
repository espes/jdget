package org.appwork.fileiconexport;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class ICONIMAGE extends Structure {
    /**
     * DIB header<br>
     * C type : BITMAPINFOHEADER
     */
    public BITMAPINFOHEADER icHeader;
    /**
     * Color table<br>
     * C type : RGBQUAD[1]
     */
    public Pointer[]        icColors = new Pointer[1];
    /**
     * DIB bits for XOR mask<br>
     * C type : BYTE[1]
     */
    public byte[]           icXOR    = new byte[1];
    /**
     * DIB bits for AND mask<br>
     * C type : BYTE[1]
     */
    public byte[]           icAND    = new byte[1];

    public ICONIMAGE(final Pointer p) {
        super(p);
        this.initFieldOrder();
        this.read();
    }

    protected void initFieldOrder() {
        this.setFieldOrder(new java.lang.String[] { "icHeader", "icColors", "icXOR", "icAND" });
    }

}
