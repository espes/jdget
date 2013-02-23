package org.appwork.swing.event;

import java.awt.AWTEvent;
import java.util.EventListener;

public interface AWTEventListener extends EventListener {

    /**
     * @param parameter
     */
    void onAWTEventAfterDispatch(AWTEvent parameter);

    /**
     * @param parameter
     */
    void onAWTEventBeforeDispatch(AWTEvent parameter);

}