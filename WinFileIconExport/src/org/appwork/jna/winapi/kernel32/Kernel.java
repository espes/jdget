package org.appwork.jna.winapi.kernel32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HRSRC;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel extends StdCallLibrary {
    Kernel                I                        = (Kernel) Native.loadLibrary("Kernel32", Kernel.class); ;

    // public short ExtractIconEx(String path, int index, HICON[] large, HICON[]
    // small, short nIcons);
    public static final int LOAD_LIBRARY_AS_DATAFILE = 0x00000002;

    /**
     * http://msdn.microsoft.com/en-us/library/ms648037(v=VS.85).aspx
     * 
     * @param lib
     * @param resType
     * @param enumResourceNamesCallback
     * @param data
     */
    public void EnumResourceNamesA(HMODULE lib, String resType, EnumResourceNamesCallback enumResourceNamesCallback, long data);

    /**
     * http://msdn.microsoft.com/en-us/library/ms648042(v=vs.85).aspx<br>
     * 
     * @return
     * 
     */
    public HRSRC FindResourceA(HMODULE lib, Pointer name, String type);

    /**
     * http://msdn.microsoft.com/en-us/library/ms683152%28v=VS.85%29.aspx
     * 
     * @param lib
     * @return
     */
    public boolean FreeLibrary(HMODULE lib);

    public HMODULE LoadLibraryExA(String path, Object fileHandle, int flags);

    /**
     * http://msdn.microsoft.com/en-us/library/ms648046(v=VS.85).aspx
     * 
     * @param module
     * @param resourceHandle
     * @return
     */
    public HGLOBAL LoadResource(HMODULE module, HRSRC resourceHandle);

    /**
     * http://msdn.microsoft.com/en-us/library/ms648047(v=VS.85).aspx
     * 
     * @param glob
     * @return
     */
    public Pointer LockResource(HGLOBAL glob);
}
