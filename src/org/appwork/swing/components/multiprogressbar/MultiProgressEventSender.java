package org.appwork.swing.components.multiprogressbar;

import org.appwork.utils.event.Eventsender;

public class MultiProgressEventSender extends Eventsender<MultiProgressListener, MultiProgressEvent> {

    @Override
    protected void fireEvent(MultiProgressListener listener, MultiProgressEvent event) {
       listener.onChanged();
    }
}