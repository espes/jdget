package org.jdownloader.gui.views.linkgrabber.addlinksdialog;

import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import jd.parser.html.HTMLParser;

import org.appwork.swing.components.ExtTextArea;

public class J17DragAndDropDelegater extends TransferHandler {

    private TransferHandler org;
    private ExtTextArea     input;

    public J17DragAndDropDelegater(ExtTextArea input) {
        org = input.getTransferHandler();
        this.input = input;
    }

    // 1.7 only @Override
    @Override
    public void setDragImage(Image img) {
        // 1.7 only
        org.setDragImage(img);
    }

    @Override
    // 1.7 only @Override
    public Image getDragImage() {
        // 1.7 only
        return org.getDragImage();
    }

    @Override
    // 1.7 only @Override
    public void setDragImageOffset(Point p) {
        // 1.7 only
        org.setDragImageOffset(p);
    }

    @Override
    // 1.7 only @Override
    public Point getDragImageOffset() {
        // 1.7 only
        return org.getDragImageOffset();
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
        boolean ret = org.importData(support);
        if (ret) {
            String txt = input.getText();
            input.setText(AddLinksDialog.list(HTMLParser.getHttpLinks(txt)));
        }
        return ret;
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
