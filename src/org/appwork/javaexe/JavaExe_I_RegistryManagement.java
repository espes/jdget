package org.appwork.javaexe;

/*****************************************************************************/
/***  (c) 2002-2012, DevWizard (DevWizard@free.fr)                         ***/
/*****************************************************************************/

/*****************************************************************************/
public interface JavaExe_I_RegistryManagement {
    static final int HKEY_CLASSES_ROOT     = 0x80000000;
    static final int HKEY_CURRENT_USER     = 0x80000001;
    static final int HKEY_LOCAL_MACHINE    = 0x80000002;
    static final int HKEY_USERS            = 0x80000003;
    static final int HKEY_PERFORMANCE_DATA = 0x80000004;
    static final int HKEY_CURRENT_CONFIG   = 0x80000005;
    static final int HKEY_DYN_DATA         = 0x80000006;

    static final int REG_NONE              = 0;
    static final int REG_SZ                = 1;
    static final int REG_EXPAND_SZ         = 2;
    static final int REG_BINARY            = 3;
    static final int REG_DWORD             = 4;
    static final int REG_DWORD_BIG_ENDIAN  = 5;
    static final int REG_LINK              = 6;
    static final int REG_MULTI_SZ          = 7;
    static final int REG_QWORD             = 11;

    /*******************************************/
    // public static native String nativeReg_GetValueSTR(int hkey, String pathKey, String nameValue, boolean isExpandVal);
    // public static native byte[] nativeReg_GetValueBIN(int hkey, String pathKey, String nameValue);
    // public static native int nativeReg_GetValueDWORD(int hkey, String pathKey, String nameValue);
    // public static native long nativeReg_GetValueQWORD(int hkey, String pathKey, String nameValue);
    // public static native String[] nativeReg_GetValueMULTI(int hkey, String pathKey, String nameValue);

    // public static native boolean nativeReg_SetValueSTR(int hkey, String pathKey, String nameValue, String val, boolean isTypeExpand);
    // public static native boolean nativeReg_SetValueBIN(int hkey, String pathKey, String nameValue, byte[] val);
    // public static native boolean nativeReg_SetValueDWORD(int hkey, String pathKey, String nameValue, int val, boolean isTypeBigEndian);
    // public static native boolean nativeReg_SetValueQWORD(int hkey, String pathKey, String nameValue, long val);
    // public static native boolean nativeReg_SetValueMULTI(int hkey, String pathKey, String nameValue, String[] val);

    // public static native int nativeReg_GetTypeValue(int hkey, String pathKey, String nameValue);

    // public static native boolean nativeReg_CreateKey(int hkey, String pathKey);
    // public static native boolean nativeReg_DeleteKey(int hkey, String pathKey);
    // public static native boolean nativeReg_DeleteValue(int hkey, String pathKey, String nameValue);

    // public static native String[] nativeReg_EnumKeys(int hkey, String pathKey);
    // public static native String[] nativeReg_EnumValues(int hkey, String pathKey);
}
