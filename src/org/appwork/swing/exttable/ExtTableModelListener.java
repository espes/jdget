package org.appwork.swing.exttable;

import java.util.EventListener;

public interface ExtTableModelListener extends EventListener {

    /**
     * @param event
     */
    void onExtTableModelEvent(ExtTableModelEventWrapper event);

}