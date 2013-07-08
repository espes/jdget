package org.jdownloader.gui.toolbar.action;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import jd.gui.swing.jdgui.interfaces.View;

import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.downloads.DownloadsView;
import org.jdownloader.gui.views.downloads.table.DownloadsTable;
import org.jdownloader.gui.views.downloads.table.DownloadsTableModel;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTable;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTableModel;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberView;

public class MoveToBottomAction extends AbstractMoveAction {

    public MoveToBottomAction() {
        setName(_GUI._.MoveToBottomAction_MoveToBottomAction());
        setIconKey("go-bottom");

        setAccelerator(KeyEvent.VK_END, InputEvent.ALT_DOWN_MASK);

    }

    @Override
    public void onGuiMainTabSwitch(View oldView, View newView) {
        if (newView instanceof DownloadsView) {
            DownloadsTable table = ((DownloadsTable) DownloadsTableModel.getInstance().getTable());
            setDelegateAction(table.getMoveToBottomAction());
        } else if (newView instanceof LinkGrabberView) {
            LinkGrabberTable table = ((LinkGrabberTable) LinkGrabberTableModel.getInstance().getTable());
            setDelegateAction(table.getMoveToBottomAction());
        } else {
            setDelegateAction(null);
        }
    }

}
