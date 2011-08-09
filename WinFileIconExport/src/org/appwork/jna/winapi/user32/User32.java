package org.appwork.jna.winapi.user32;

import org.appwork.jna.winapi.structs.IconInfo;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.win32.StdCallLibrary;

public interface User32 extends StdCallLibrary {
    /** The instance. */
    final static User32 I = (User32) Native.loadLibrary("user32", User32.class);

    /**
     * http://msdn.microsoft.com/en-us/library/ms648070(v=VS.85).aspx
     * 
     * @param hIcon
     * @param iconinfo
     * @return
     */
    boolean GetIconInfo(HICON hIcon, IconInfo iconinfo);

}
