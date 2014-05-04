package org.appwork.fileiconexport;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.StdCallLibrary;

public interface Explorerframe extends StdCallLibrary {

    HRESULT SetProgressStateA(HWND h, int i);

}
