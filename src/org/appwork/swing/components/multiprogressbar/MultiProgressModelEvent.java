package org.appwork.swing.components.multiprogressbar;

import org.appwork.utils.event.DefaultEvent;

public class MultiProgressModelEvent extends DefaultEvent {

    public static enum Type {
    }

    public MultiProgressModelEvent(MultiProgressModel caller) {
        super(caller);
    }
}