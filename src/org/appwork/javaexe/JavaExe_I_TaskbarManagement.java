package org.appwork.javaexe;

/*****************************************************************************/
/***  (c) 2002-2012, DevWizard (DevWizard@free.fr)                         ***/
/*****************************************************************************/

/*****************************************************************************/
public interface JavaExe_I_TaskbarManagement {
    static final int ACT_CLICK_NOP    = 0;
    static final int ACT_CLICK_OPEN   = 1;
    static final int ACT_CLICK_MENU   = 2;

    static final int NIIF_NONE        = 0;
    static final int NIIF_INFO        = 1;
    static final int NIIF_WARNING     = 2;
    static final int NIIF_ERROR       = 3;
    static final int NIIF_USER        = 4;

    static final int MFT_MENUBARBREAK = 0x0020;
    static final int MFT_MENUBREAK    = 0x0040;
    static final int MFT_RADIOCHECK   = 0x0200;
    static final int MFT_SEPARATOR    = 0x0800;
    static final int MFT_RIGHTORDER   = 0x2000;

    static final int MFS_DISABLED     = 0x0003;
    static final int MFS_CHECKED      = 0x0008;
    static final int MFS_HILITE       = 0x0080;

    /*******************************************/
    // public static String[][] taskGetMenu(boolean isRightClick, int menuID);
    // public static int taskGetDefaultMenuID(boolean isRightClick);

    // public static void taskDoAction(boolean isRightClick, int menuID);
    // public static boolean taskDisplayMenu(boolean isRightClick, Component parent, int x, int y);

    // public static String[] taskGetInfo();
    // public static boolean taskIsShow();
    // public static void taskInit();

    // public static void taskDoBalloonAction();
    // public static boolean taskIsBalloonShow();
    // public static void taskSetBalloonSupported(boolean isSupported);
    // public static String[] taskGetBalloonInfo();

    // public static void taskDataFromService(Serializable data);
    // public static boolean taskIsDataForService();
    // public static Serializable taskDataForService();
}
