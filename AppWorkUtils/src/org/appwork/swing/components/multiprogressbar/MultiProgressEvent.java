package org.appwork.swing.components.multiprogressbar;

import org.appwork.utils.event.DefaultEvent;

public class MultiProgressEvent extends DefaultEvent {

 
    /**
     * @param multiProgressBar
     */
    public MultiProgressEvent(MultiProgressBar multiProgressBar) {
        super(multiProgressBar);
    }
}