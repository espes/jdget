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

import javax.swing.TransferHandler.TransferSupport;

/**
 * @author daniel
 * 
 */
public class RemoveMEDataFlavor {
    public static DataFlavor REMOVEME = new DataFlavor(RemoveMEDataFlavor.class, RemoveMEDataFlavor.class.getName());

    public static boolean removeMe(TransferSupport info) {
        return removeMe(info.getTransferable());
    }

    public static boolean removeMe(Transferable info) {
        if (info == null) return false;
        try {
            if (info.isDataFlavorSupported(REMOVEME) && info.getTransferData(REMOVEME) == Boolean.TRUE) return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean supported(TransferSupport info) {
        return supported(info.getTransferable());
    }

    public static boolean supported(Transferable info) {
        if (info == null) return false;
        try {
            if (info.isDataFlavorSupported(REMOVEME)) return true;
        } catch (Exception e) {
        }
        return false;
    }
}
