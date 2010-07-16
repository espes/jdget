/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author daniel
 * 
 */
public class EmptyTransferable implements Transferable {

    public static final EmptyTransferable Empty = new EmptyTransferable();

    public EmptyTransferable() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
     * .DataFlavor)
     */

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        throw new UnsupportedFlavorException(flavor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @seejava.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
     * datatransfer.DataFlavor)
     */

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
    }

}
