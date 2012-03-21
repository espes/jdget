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

package jd.gui.swing.jdgui.views.settings.panels.addons;

import jd.gui.UserIO;
import jd.gui.swing.jdgui.menu.AddonsMenu;

import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtCheckColumn;
import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.extensions.LazyExtension;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.translate._JDT;

public class ActivateColumn extends ExtCheckColumn<LazyExtension> {

    private static final long      serialVersionUID = 658156218405204887L;
    private final ExtensionManager addons;

    public ActivateColumn(String name, ExtTableModel<LazyExtension> table, ExtensionManager addons) {
        super(name, table);

        this.addons = addons;
    }

    @Override
    public boolean isEditable(LazyExtension obj) {
        return true;
    }

    @Override
    protected boolean getBooleanValue(LazyExtension value) {
        return value._isEnabled();
    }

    @Override
    protected void setBooleanValue(boolean value, LazyExtension object) {
        if (value == object._isEnabled()) return;
        if (value) {
            try {
                object._setEnabled(true);

                if (object._getExtension().getGUI() != null) {
                    int ret = UserIO.getInstance().requestConfirmDialog(UserIO.DONT_SHOW_AGAIN, object.getName(), _JDT._.gui_settings_extensions_show_now(object.getName()));

                    if (UserIO.isOK(ret)) {
                        // activate panel
                        object._getExtension().getGUI().setActive(true);
                        // bring panel to front
                        object._getExtension().getGUI().toFront();

                    }
                }
            } catch (StartException e) {
                Dialog.getInstance().showExceptionDialog(_JDT._.dialog_title_exception(), e.getMessage(), e);
            } catch (StopException e) {
                e.printStackTrace();
            }
        } else {
            try {

                object._setEnabled(false);
            } catch (StartException e) {
                e.printStackTrace();
            } catch (StopException e) {
                Dialog.getInstance().showExceptionDialog(_JDT._.dialog_title_exception(), e.getMessage(), e);
            }
        }
        /*
         * we save enabled/disabled status here, plugin must be running when
         * enabled
         */

        AddonsMenu.getInstance().onUpdated();

        // ConfigSidebar.getInstance(null).updateAddons();
        addons.updateShowcase();
    }

}
