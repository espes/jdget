package org.appwork.uio;

import org.appwork.utils.swing.dialog.Dialog;

public class UIOManager {

    private static UserIOHandlerInterface USERIO = new BasicDialogHandler();

    public static void setUserIO(final UserIOHandlerInterface io) {
        USERIO = io;
    }

    public static UserIOHandlerInterface I() {
        return USERIO;
    }

    /**
     * Use this flag to show display of the Timer
     */

    public static final int LOGIC_COUNTDOWN                      = 1 << 2;
    /**
     * Don't show again is only valid for this session, but is not saved for
     * further sessions
     */

    public static final int LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT = 1 << 11;
    /**
     * Hide the cancel Button
     */

    public static final int BUTTONS_HIDE_CANCEL                  = 1 << 4;
    /**
     * Hide the OK button
     */

    public static final int BUTTONS_HIDE_OK                      = 1 << 3;
    /**
     * Often, the {@link Dialog#STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not
     * make sense for the cancel option. Use this flag if the option should be
     * ignored if the user selects Cancel
     */

    public static final int LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL = 1 << 9;
    /**
     * Often, the {@link Dialog#STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not
     * make sense for the ok option. Use this flag if the option should be
     * ignored if the user selects OK
     */

    public static final int LOGIC_DONT_SHOW_AGAIN_IGNORES_OK     = 1 << 10;

}
