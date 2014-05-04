package org.appwork.jna.winapi.kernel32;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public interface EnumResourceNamesCallback extends StdCallCallback {

    /** Return whether to continue enumeration. */

    boolean callback(HMODULE module, Pointer type, int name, Pointer data);

}
