package org.appwork.jna.winapi.shell32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.win32.StdCallLibrary;

public interface Shell32 extends StdCallLibrary {
    Shell32 I = (Shell32) Native.loadLibrary("shell32", Shell32.class); ;

    public short ExtractIconEx(String path, int index, HICON[] large, HICON[] small, short nIcons);
}
