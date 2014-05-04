package org.appwork.swing.exttable.utils;

import javax.swing.Action;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.appwork.swing.exttable.ExtTable;

public class MinimumSelectionObserver implements ListSelectionListener {

    protected final ExtTable<?> table;
    protected final Action      action;
    protected final int         minSelections;

    /**
     * @param table
     * @param action
     * @param minSelections
     */
    public MinimumSelectionObserver(final ExtTable<?> table, final Action action, final int minSelections) {
        this.table = table;
        this.action = action;
        this.minSelections = minSelections;
        action.setEnabled(table.getSelectedRowCount() >= minSelections);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
     * .ListSelectionEvent)
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) { return; }
        this.action.setEnabled(this.table.getSelectedRowCount() >= this.minSelections);

    }

}
