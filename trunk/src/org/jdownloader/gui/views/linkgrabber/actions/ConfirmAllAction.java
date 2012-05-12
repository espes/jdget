package org.jdownloader.gui.views.linkgrabber.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import jd.controlling.IOEQ;
import jd.controlling.packagecontroller.AbstractNode;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTableModel;
import org.jdownloader.images.NewTheme;

public class ConfirmAllAction extends AppAction {
    /**
     * 
     */
    private static final long serialVersionUID = 4794612717641894527L;

    private boolean           autostart;

    public ConfirmAllAction(boolean autostart) {
        if (autostart) {
            setName(_GUI._.ConfirmAction_ConfirmAction_context_add_and_start());
            Image add = NewTheme.I().getImage("media-playback-start", 20);
            Image play = NewTheme.I().getImage("add", 12);
            setSmallIcon(new ImageIcon(ImageProvider.merge(add, play, 0, 0, 9, 10)));
            this.autostart = true;
        } else {
            setName(_GUI._.ConfirmAction_ConfirmAction_context_add());
            setSmallIcon(NewTheme.I().getIcon("go-next", 20));
            this.autostart = false;
        }

    }

    public ConfirmAllAction() {
        this(false);
    }

    public void actionPerformed(ActionEvent e) {
        IOEQ.add(new Runnable() {

            public void run() {

                ArrayList<AbstractNode> packages = new ArrayList<AbstractNode>(LinkGrabberTableModel.getInstance().getAllPackageNodes());

                new ConfirmAction(autostart, packages).actionPerformed(null);
            }

        }, true);
    }

}
