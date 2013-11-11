package org.appwork.swing.exttable;

import java.util.EventListener;

public interface ExtTableModelListener extends EventListener {

    /**
     * @param listener
     */
    void onExtTableModelEvent(ExtTableModelListener listener);

}