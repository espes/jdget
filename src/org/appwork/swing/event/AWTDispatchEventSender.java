package org.appwork.swing.event;

import java.awt.AWTEvent;

import org.appwork.utils.event.Eventsender;

public class AWTDispatchEventSender extends Eventsender<AWTEventListener, AWTDispatchEvent> {

    @Override
    protected void fireEvent(AWTEventListener listener, AWTDispatchEvent event) {
        switch (event.getType()) {
        case POST_DISPATCH:
            listener.onAWTEventAfterDispatch((AWTEvent) event.getParameter());
            break;
        case PRE_DISPATCH:
            listener.onAWTEventBeforeDispatch((AWTEvent) event.getParameter());
           break;
        default:
            System.out.println("Unhandled Event: " + event);
        }
    }
}