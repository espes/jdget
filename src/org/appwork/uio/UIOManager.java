package org.appwork.uio;



public class UIOManager {

    private static UserIOHandlerInterface USERIO = null;

    public static void setUserIO(UserIOHandlerInterface io) {
        USERIO = io;
    }

    public static UserIOHandlerInterface I() {
        return USERIO;
    }

}
