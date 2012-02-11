/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * @author thomas
 * 
 */
public class DragDropHelper extends TransferHandler implements DropTargetListener {

    /**
     * 
     */
    private static final long serialVersionUID = 648362975715129386L;

    @Override
    public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean canImport(final TransferSupport support) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        // TODO Auto-generated method stub
        return new StringSelection(this.getClass().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent
     * )
     */
    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    @Override
    public void dragExit(final DropTargetEvent dte) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent
     * )
     */
    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    @Override
    public void drop(final DropTargetDropEvent dtde) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.
     * DropTargetDragEvent)
     */
    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

}
