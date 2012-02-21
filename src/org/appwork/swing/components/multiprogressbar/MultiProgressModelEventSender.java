package org.appwork.swing.components.multiprogressbar;

import org.appwork.utils.event.Eventsender;

public class MultiProgressModelEventSender extends Eventsender<MultiProgressModelListener, MultiProgressModelEvent> {

    @Override
    protected void fireEvent(MultiProgressModelListener listener, MultiProgressModelEvent event) {
        listener.onChanged();
    }
}