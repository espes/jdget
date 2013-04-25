package org.appwork.swing.event;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

import org.appwork.utils.Application;
import org.appwork.utils.swing.EDTRunner;

/**
 * This class creates a copy paste cut select delete contecxtmenu for every
 * textcomponent.
 * 
 * @author $Author: unknown$
 */
public class AWTEventQueueLinker extends EventQueue {

    private static AWTEventQueueLinker INSTANCE;

    /**
     * @return
     */
    public static AWTEventQueueLinker getInstance() {
        // TODO Auto-generated method stub
        return AWTEventQueueLinker.INSTANCE;
    }

    public static boolean isLinked() {
        return AWTEventQueueLinker.INSTANCE != null;
    }

    public synchronized static void link() {
        if (AWTEventQueueLinker.INSTANCE != null) { return; }
        AWTEventQueueLinker.INSTANCE = new AWTEventQueueLinker();
    }

    private final AWTDispatchEventSender eventSender;

    private AWTEventQueueLinker() {
        super();
        this.eventSender = new AWTDispatchEventSender();
        /* http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7097333 */
        if (Application.getJavaVersion() >= Application.JAVA17) {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    /* under JDK1.7 we have to push EventQueue inside EDT */
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(AWTEventQueueLinker.this);
                }
            };
        } else {
            new Thread("EventQueuePushWorkaround") {
                @Override
                public void run() {
                    /* under java <=1.6 we have to push EventQueue outside EDT */
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(AWTEventQueueLinker.this);
                };
            }.start();
        }
    }

    @Override
    protected void dispatchEvent(final AWTEvent event) {
        this.eventSender.fireEvent(new AWTDispatchEvent(this, AWTDispatchEvent.Type.PRE_DISPATCH, event));
        super.dispatchEvent(event);
        this.eventSender.fireEvent(new AWTDispatchEvent(this, AWTDispatchEvent.Type.POST_DISPATCH, event));

    }

    public AWTDispatchEventSender getEventSender() {
        return this.eventSender;
    }
}
