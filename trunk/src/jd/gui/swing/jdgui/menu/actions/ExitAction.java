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

import org.jdownloader.gui.shortcuts.ShortcutController;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.updatev2.RestartController;

public class ExitAction extends ActionAdapter {

    private static final long serialVersionUID = -1428029294638573437L;

    public ExitAction() {
        super(_GUI._.action_exit(), "action.exit", "exit");
    }

    @Override
    public void onAction(ActionEvent e) {
        RestartController.getInstance().exitAsynch();
    }

    @Override
    public void initDefaults() {
    }

    @Override
    public String createAccelerator() {
        return ShortcutController._.getExitJDownloaderAction();
    }

    @Override
    public String createTooltip() {
        return _GUI._.action_exit_tooltip();
    }

}
