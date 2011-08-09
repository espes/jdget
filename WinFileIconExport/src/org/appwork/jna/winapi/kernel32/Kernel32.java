package org.appwork.jna.winapi.kernel32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {
    Kernel32                I                        = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class); ;

    // public short ExtractIconEx(String path, int index, HICON[] large, HICON[]
    // small, short nIcons);
    public static final int LOAD_LIBRARY_AS_DATAFILE = 0x00000002;

    public HMODULE LoadLibraryExA(String path, Object fileHandle, int flags);
}
