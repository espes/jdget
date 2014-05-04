/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * @author Thomas
 * 
 */
public class ExtTransferable implements Transferable {

    private ExtDataFlavor<?> flavor;
    private List<?>  items;
    private DataFlavor[] flavorList;

    /**
     * @param selectedObjects
     */
    public ExtTransferable(ExtDataFlavor<?> flavor,List<?> selectedObjects) {
        // TODO Auto-generated constructor stub
        items = selectedObjects;
        this.flavor = flavor;
        flavorList=new DataFlavor[] { flavor };
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        // TODO Auto-generated method stub
        return flavorList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
     * datatransfer.DataFlavor)
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // TODO Auto-generated method stub
        return this.flavor.equals(flavor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
     * .DataFlavor)
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (this.flavor.equals(flavor)) { return items; }
        return null;
    }

}
