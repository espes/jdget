package org.appwork.fileiconexport;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinGDI.RGBQUAD;

public class IMAGEICON extends Structure {
    public static IMAGEICON getInstance(final Pointer pimageIcon) {
        final IMAGEICON s = (IMAGEICON) Structure.newInstance(IMAGEICON.class);

        s.useMemory(pimageIcon);
        // final Short ret = (Short) s.readField("idCount");
        // s.idEntries = new GRPICONDIRENTRY[ret];
        s.autoRead();
        return s;
    }

    /*
     * typdef struct { BITMAPINFOHEADER icHeader; // DIB header RGBQUAD
     * icColors[1]; // Color table BYTE icXOR[1]; // DIB bits for XOR mask BYTE
     * icAND[1]; // DIB bits for AND mask } ICONIMAGE, *LPICONIMAGE;
     */
    public BITMAPINFOHEADER icHeader;
    public RGBQUAD[]        bmiColors = new RGBQUAD[1];
    public byte[]           icXOR     = new byte[1];
    public byte[]           icAND     = new byte[1];

}
