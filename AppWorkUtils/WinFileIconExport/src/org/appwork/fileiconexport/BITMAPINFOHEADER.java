package org.appwork.fileiconexport;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class BITMAPINFOHEADER extends Structure {
    public static class ByReference extends BITMAPINFOHEADER implements Structure.ByReference {

    }

    public static class ByValue extends BITMAPINFOHEADER implements Structure.ByValue {

    }

    public int        biSize;
    public NativeLong biWidth;
    public NativeLong biHeight;
    public short      biPlanes;
    public short      biBitCount;
    public int        biCompression;
    public int        biSizeImage;
    public NativeLong biXPelsPerMeter;
    public NativeLong biYPelsPerMeter;
    public int        biClrUsed;

    public int        biClrImportant;

    public BITMAPINFOHEADER() {
        super();
        this.initFieldOrder();
    }

    public BITMAPINFOHEADER(final Pointer pIcon) {
        super(pIcon);
        this.initFieldOrder();
        this.read();

    };

    protected void initFieldOrder() {
        this.setFieldOrder(new java.lang.String[] { "biSize", "biWidth", "biHeight", "biPlanes", "biBitCount", "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter", "biClrUsed", "biClrImportant" });
    };
}
