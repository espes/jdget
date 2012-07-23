package org.jdownloader.gui.views.linkgrabber.addlinksdialog;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import jd.controlling.ClipboardMonitoring;
import jd.parser.html.HTMLParser;

import org.appwork.swing.components.ExtTextArea;
import org.appwork.utils.StringUtils;
import org.jdownloader.logging.LogController;

public class DragAndDropDelegater extends TransferHandler {

    private TransferHandler org;
    private ExtTextArea     input;
    private AddLinksDialog  dialog;

    public DragAndDropDelegater(AddLinksDialog addLinksDialog, ExtTextArea input) {
        org = input.getTransferHandler();
        this.input = input;
        dialog = addLinksDialog;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        org.exportAsDrag(comp, e, action);
    }

    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
        org.exportToClipboard(comp, clip, action);
    }

    @Override
    public boolean importData(TransferSupport support) {
        String html;
        try {
            html = ClipboardMonitoring.getHTMLTransferData(support.getTransferable());

            String text = ClipboardMonitoring.getStringTransferData(support.getTransferable());
            // boolean ret = org.importData(support);

            String base = ClipboardMonitoring.getCurrentBrowserURL(support.getTransferable());

            StringBuilder sb = new StringBuilder();
            if (!StringUtils.isEmpty(html)) {
                sb.append(html);
            }

            if (!StringUtils.isEmpty(text)) {
                if (sb.length() > 0) sb.append("\r\n");
                sb.append(text);
            }

            String txt = sb.toString();
            dialog.parse(txt);
            String[] list = HTMLParser.getHttpLinks(txt, base);
            if (list.length == 0) {
                list = HTMLParser.getHttpLinks(txt.replace("www.", "http://www."), base);
            }
            if (list.length == 0) {
                list = HTMLParser.getHttpLinks("http://" + txt, base);
            }

            if (list.length == 0) {
                input.setText(txt);
            } else {
                String parsed = AddLinksDialog.list(list);
                input.setText(parsed);
            }

            return true;

        } catch (Throwable e) {
            LogController.CL().log(e);
        }
        return false;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        return org.importData(comp, t);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return org.canImport(support);
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return org.canImport(comp, transferFlavors);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return org.getSourceActions(c);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return org.getVisualRepresentation(t);
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        try {
            Method method = TransferHandler.class.getDeclaredMethod("createTransferable", new Class[] { JComponent.class });
            method.setAccessible(true);
            return (Transferable) method.invoke(org, new Object[] { c });

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        try {
            Method method = TransferHandler.class.getDeclaredMethod("exportDone", new Class[] { JComponent.class, Transferable.class, int.class });
            method.setAccessible(true);
            method.invoke(org, new Object[] { source, data, action });

        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

    }

}
