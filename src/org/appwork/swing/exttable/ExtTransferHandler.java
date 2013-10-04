package org.appwork.swing.exttable;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class ExtTransferHandler<T> extends TransferHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -6250155503485735869L;

    public ExtTransferHandler() {

    }

    private ExtTable<T> table;

    public ExtTable<T> getTable() {
        return table;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(final TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        final JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();       
        if (dl.isInsertRow()) {
            final int dropRow = dl.getRow();
            try {
                return table.getModel().move((java.util.List<T>) support.getTransferable().getTransferData(table.getDataFlavor()), dropRow);

            } catch (final UnsupportedFlavorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean canImport(final TransferSupport support) {
   
        if (support.isDrop()) {
            return support.isDataFlavorSupported(table.getDataFlavor());

        }
        return false;
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.MOVE;
    }

    public void setTable(final ExtTable<T> table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        return new ExtTransferable(table.getDataFlavor(), table.getModel().getSelectedObjects());

    }
}
