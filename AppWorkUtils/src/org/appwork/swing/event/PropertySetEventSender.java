package org.appwork.swing.event;

import java.awt.Component;

import org.appwork.utils.event.Eventsender;

public class PropertySetEventSender extends Eventsender<PropertySetListener, PropertySetEvent> {

    @Override
    protected void fireEvent(final PropertySetListener listener, final PropertySetEvent event) {
        switch (event.getType()) {
        case SET:
            listener.onPropertySet((Component) event.getCaller(),(String)event.getParameter(0),event.getParameter(1),event.getParameter(2));
            break;
        // fill
        default:
            System.out.println("Unhandled Event: " + event);
        }
    }
}