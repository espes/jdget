//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.swing.jdgui.menu.actions;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import jd.controlling.IOEQ;
import jd.controlling.downloadcontroller.DownloadController;
import jd.gui.UserIO;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;

import org.jdownloader.gui.translate._GUI;

public class CleanupDownloads extends ActionAdapter {

    private static final long serialVersionUID = -7185006215784212976L;

    public CleanupDownloads() {
        super(_GUI._.action_remove_links(), "action.remove.links", "remove");
    }

    @Override
    public void onAction(ActionEvent e) {
        IOEQ.add(new Runnable() {

            public void run() {
                if (!UserIO.isOK(UserIO.getInstance().requestConfirmDialog(UserIO.DONT_SHOW_AGAIN | UserIO.DONT_SHOW_AGAIN_IGNORES_CANCEL, _GUI._.jd_gui_swing_jdgui_menu_actions_CleanupDownload_message()))) return;

                DownloadController dlc = DownloadController.getInstance();

                LinkedList<DownloadLink> downloadstodelete = new LinkedList<DownloadLink>();
                final boolean readL = dlc.readLock();
                try {
                    for (FilePackage fp : dlc.getPackages()) {
                        downloadstodelete.addAll(dlc.getDownloadLinksbyStatus(fp, LinkStatus.FINISHED | LinkStatus.ERROR_ALREADYEXISTS));
                    }
                } finally {
                    dlc.readUnlock(readL);
                }
                for (DownloadLink dl : downloadstodelete) {
                    dl.getFilePackage().remove(dl);
                }
            }
        });
    }

    @Override
    public void initDefaults() {
    }

    @Override
    public String createMnemonic() {
        return _GUI._.action_remove_links_mnemonic();
    }

    @Override
    public String createAccelerator() {
        return _GUI._.action_remove_links_accelerator();
    }

    @Override
    public String createTooltip() {
        return _GUI._.action_remove_links_tooltip();
    }

}