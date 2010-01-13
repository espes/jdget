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
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.TransferHandler.TransferSupport;

public class ClipboardUtils {

    private final static ClipboardUtils INSTANCE = new ClipboardUtils();

    private DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
    private DataFlavor explorerFlavor;
    private DataFlavor uriListFlavor;

    private static ArrayList<Object> cutpasteBuffer = new ArrayList<Object>();

    public static ClipboardUtils getInstance() {
        return INSTANCE;
    }

    private ClipboardUtils() {
        try {
            uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
        } catch (ClassNotFoundException e) {
            uriListFlavor = null;
        }
        try {
            explorerFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList");
        } catch (ClassNotFoundException e) {
            explorerFlavor = null;
        }
    }

    public DataFlavor getExplorerFlavor() {
        return explorerFlavor;
    }

    public static void putToCutPasteBuffer(ArrayList<Object> objs) {
        synchronized (cutpasteBuffer) {
            cutpasteBuffer.clear();
            if (objs == null || objs.size() == 0) return;
            cutpasteBuffer.addAll(objs);
        }
    }

    public static ArrayList<Object> getCutPasteBuffer() {
        synchronized (cutpasteBuffer) {
            return new ArrayList<Object>(cutpasteBuffer);
        }
    }

    public static boolean emptyCutPasteBuffer() {
        synchronized (cutpasteBuffer) {
            return cutpasteBuffer.isEmpty();
        }
    }

    public boolean hasSupport(TransferSupport info) {
        if (info != null) {
            if (uriListFlavor != null && info.isDataFlavorSupported(uriListFlavor)) return true;
            if (fileListFlavor != null && info.isDataFlavorSupported(fileListFlavor)) return true;
            if (explorerFlavor != null && info.isDataFlavorSupported(explorerFlavor)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<File> getFiles(TransferSupport info) {
        ArrayList<File> files = new ArrayList<File>();
        if (info != null) {
            try {
                if (info.isDataFlavorSupported(fileListFlavor)) {
                    List list = (List) info.getTransferable().getTransferData(fileListFlavor);
                    ListIterator it = list.listIterator();
                    while (it.hasNext()) {
                        File f = (File) it.next();
                        if (f.exists()) files.add(f);
                    }
                } else if (uriListFlavor != null && info.isDataFlavorSupported(uriListFlavor)) {
                    StringTokenizer izer = new StringTokenizer((String) info.getTransferable().getTransferData(uriListFlavor), "\r\n");
                    while (izer.hasMoreTokens()) {
                        URI fi = new URI(izer.nextToken());
                        File f = new File(fi.getPath());
                        if (f.exists()) files.add(f);
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return files;
    }
}
