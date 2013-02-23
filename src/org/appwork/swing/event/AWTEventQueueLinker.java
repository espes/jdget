package org.appwork.swing.event;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

/**
 * This class creates a copy paste cut select delete contecxtmenu for every
 * textcomponent.
 * 
 * @author $Author: unknown$
 */
public class AWTEventQueueLinker extends EventQueue {

    private static AWTEventQueueLinker INSTANCE;

    public static void link() {
        INSTANCE = new AWTEventQueueLinker();

    }

    public static boolean isLinked() {
        return INSTANCE != null;
    }

    private AWTDispatchEventSender eventSender;

    private AWTEventQueueLinker() {
        super();
        eventSender = new AWTDispatchEventSender();
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);

    }

    @Override
    protected void dispatchEvent(AWTEvent event) {
        eventSender.fireEvent(new AWTDispatchEvent(this, AWTDispatchEvent.Type.PRE_DISPATCH, event));
        super.dispatchEvent(event);
        eventSender.fireEvent(new AWTDispatchEvent(this, AWTDispatchEvent.Type.POST_DISPATCH, event));

    }

    public AWTDispatchEventSender getEventSender() {
        return eventSender;
    }

    /**
     * @return
     */
    public static AWTEventQueueLinker getInstance() {
        // TODO Auto-generated method stub
        return INSTANCE;
    }
}
