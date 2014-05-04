package org.appwork.swing.exttable;

import org.appwork.utils.event.Eventsender;

public class ExtTableModelEventSender extends Eventsender<ExtTableModelListener, ExtTableModelEvent> {

    @Override
    protected void fireEvent(final ExtTableModelListener listener, final ExtTableModelEvent event) {
        event.fire(listener);
    }
}