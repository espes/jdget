package org.jdownloader.gui.views.linkgrabber.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkOrigin;
import jd.controlling.linkcollector.LinkOriginDetails;
import jd.nutils.io.JDFileFilter;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.appwork.utils.swing.dialog.ExtFileChooserDialog;
import org.appwork.utils.swing.dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.FileChooserType;
import org.jdownloader.controlling.contextmenu.CustomizableAppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.plugins.controller.container.ContainerPluginController;

public class AddContainerAction extends CustomizableAppAction {
    /**
     * @param selection
     *            TODO
     * 
     */
    public AddContainerAction() {

        setIconKey("addContainer");
        setName(_GUI._.AddContainerAction());
        setAccelerator(KeyEvent.VK_O);
    }

    private static final long serialVersionUID = -1758454550263991986L;

    @Override
    public void setEnabled(boolean newValue) {
        super.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {

        try {
            String exts = ContainerPluginController.getInstance().getContainerExtensions(null);

            ExtFileChooserDialog d = new ExtFileChooserDialog(0, _GUI._.AddContainerAction_actionPerformed_(), null, null);
            d.setFileSelectionMode(FileChooserSelectionMode.FILES_ONLY);
            d.setFileFilter(new JDFileFilter(_GUI._.AddContainerAction_actionPerformed_extensions(exts), exts, true));
            d.setType(FileChooserType.OPEN_DIALOG);
            d.setMultiSelection(true);
            Dialog.I().showDialog(d);

            File[] filterFiles = d.getSelection();
            if (filterFiles == null) return;

            final StringBuilder sb = new StringBuilder();
            for (File r : filterFiles) {
                if (sb.length() > 0) {
                    sb.append("\r\n");
                }
                sb.append("file://");
                sb.append(r.getAbsolutePath());
            }
            LinkCollector.getInstance().addCrawlerJob(new LinkCollectingJob(new LinkOriginDetails(LinkOrigin.ADD_CONTAINER_ACTION, null), sb.toString()));
        } catch (DialogNoAnswerException e1) {
        }
    }

}
