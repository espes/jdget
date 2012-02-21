package org.appwork.swing.components.multiprogressbar;

import org.appwork.utils.event.DefaultEvent;
import org.appwork.utils.event.SimpleEvent;

public class MultiProgressModelEvent extends DefaultEvent {

    public static enum Type {
    }

    public MultiProgressModelEvent(MultiProgressModel caller) {
        super(caller);
    }
}