package org.appwork.uio;



public class UIOManager {

    private static UserIOHandlerInterface USERIO = new BasicDialogHandler();

    public static void setUserIO(final UserIOHandlerInterface io) {
        USERIO = io;
    }

    public static UserIOHandlerInterface I() {
        return USERIO;
    }

}
