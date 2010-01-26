/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import org.appwork.utils.ClipboardUtils;

/**
 * @author daniel
 * 
 */
public class ArrayListTransferable implements Transferable {

    private ArrayList<Object> objs = null;

    public ArrayListTransferable(ArrayList<Object> objs) {
        this.objs = objs;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor != null && flavor == ClipboardUtils.arrayListFlavor) {
            return objs;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        if (ClipboardUtils.arrayListFlavor == null) return new DataFlavor[] {};
        return new DataFlavor[] { ClipboardUtils.arrayListFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor != null && flavor == ClipboardUtils.arrayListFlavor) return true;
        return false;
    }

}
