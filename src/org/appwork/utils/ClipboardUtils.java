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

import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.parser.HTMLParser;

public class ClipboardUtils {

    private static ClipboardUtils INSTANCE = new ClipboardUtils();

    public final static DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
    public final static DataFlavor stringFlavor = DataFlavor.stringFlavor;
    private final static byte[] tmpByteArray = new byte[0];
    public final static DataFlavor arrayListFlavor;
    public final static DataFlavor uriListFlavor;

    static {
        DataFlavor tmp;
        try {
            tmp = new DataFlavor("text/uri-list; class=java.lang.String");
        } catch (Throwable e) {
            tmp = null;
        }
        uriListFlavor = tmp;
        try {
            tmp = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList");
        } catch (Throwable e) {
            tmp = null;
        }
        arrayListFlavor = tmp;
    }

    private ArrayList<Object> cutpasteBuffer = new ArrayList<Object>();

    public static ClipboardUtils getInstance() {
        return INSTANCE;
    }

    public static void setInstance(ClipboardUtils instance) {
        INSTANCE = instance;
    }

    public ClipboardUtils() {
    }

    public void putToCutPasteBuffer(ArrayList<Object> objs) {
        synchronized (cutpasteBuffer) {
            cutpasteBuffer.clear();
            if (objs == null || objs.size() == 0) return;
            cutpasteBuffer.addAll(objs);
        }
    }

    public ArrayList<Object> getCutPasteBuffer() {
        synchronized (cutpasteBuffer) {
            return new ArrayList<Object>(cutpasteBuffer);
        }
    }

    public boolean emptyCutPasteBuffer() {
        synchronized (cutpasteBuffer) {
            return cutpasteBuffer.isEmpty();
        }
    }

    public static boolean hasSupport(TransferSupport info) {
        if (info != null) {
            if (uriListFlavor != null && info.isDataFlavorSupported(uriListFlavor)) return true;
            if (fileListFlavor != null && info.isDataFlavorSupported(fileListFlavor)) return true;
            if (arrayListFlavor != null && info.isDataFlavorSupported(arrayListFlavor)) return true;
        }
        return false;
    }

    public static ArrayList<String> getLinks(TransferSupport trans) {
        ArrayList<String> links = new ArrayList<String>();
        String content = null;
        DataFlavor htmlFlavor = null;
        /*
         * workaround for https://bugzilla.mozilla.org/show_bug.cgi?id=385421
         */
        for (final DataFlavor flav : trans.getTransferable().getTransferDataFlavors()) {
            if (flav.getMimeType().contains("html") && flav.getRepresentationClass().isInstance(tmpByteArray)) {
                if (htmlFlavor != null) htmlFlavor = flav;
                final String charSet = new Regex(flav.toString(), "charset=(.*?)]").getMatch(0);
                if (charSet != null && charSet.equalsIgnoreCase("UTF-8")) {
                    /* we found utf-8 encoding, so lets use that */
                    htmlFlavor = flav;
                    break;
                }
            }
        }
        try {
            if (htmlFlavor != null) {
                final String charSet = new Regex(htmlFlavor.toString(), "charset=(.*?)]").getMatch(0);
                byte[] html = (byte[]) trans.getTransferable().getTransferData(htmlFlavor);
                if (CrossSystem.isLinux()) {
                    /*
                     * workaround for
                     * https://bugzilla.mozilla.org/show_bug.cgi?id=385421if
                     */
                    final int htmlLength = html.length;
                    final byte[] html2 = new byte[htmlLength];

                    int o = 0;
                    for (int i = 6; i < htmlLength - 1; i++) {
                        if (html[i] != 0) {
                            html2[o++] = html[i];
                        }
                    }
                    html = html2;
                    content = new String(html, "UTF-8");
                } else {
                    if (charSet != null) {
                        content = new String(html, charSet);
                    } else {
                        content = new String(html);
                    }
                }
            } else {
                /* try stringFlavor */
                if (trans.isDataFlavorSupported(stringFlavor)) content = ((String) trans.getTransferable().getTransferData(stringFlavor));
            }
            if (content != null) {
                links.addAll(HTMLParser.findUrls(content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return links;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<File> getFiles(TransferSupport info) {
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
