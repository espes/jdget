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

package jd.gui.swing.jdgui.menu;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jd.controlling.IOEQ;

import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.ExtensionControllerListener;
import org.jdownloader.extensions.LazyExtension;
import org.jdownloader.translate._JDT;

public class AddonsMenu extends JMenu implements ExtensionControllerListener {

    private static final long serialVersionUID = 1019851981865519325L;
    private static AddonsMenu INSTANCE         = null;

    private AddonsMenu() {
        super(_JDT._.gui_menu_extensions());
        ExtensionController.getInstance().getEventSender().addListener(this);

    }

    public static AddonsMenu getInstance() {
        if (INSTANCE == null) INSTANCE = new AddonsMenu();
        return INSTANCE;
    }

    private void updateMenu() {

        ArrayList<JMenuItem> itemsWithSubmenu = new ArrayList<JMenuItem>();
        ArrayList<JMenuItem> itemsToggle = new ArrayList<JMenuItem>();
        ArrayList<JMenuItem> itemsPress = new ArrayList<JMenuItem>();
        List<LazyExtension> pluginsOptional = ExtensionController.getInstance().getExtensions();

        for (final LazyExtension wrapper : pluginsOptional) {

            if (wrapper._isEnabled()) {
                final AbstractExtension<?> plg = wrapper._getExtension();

                ArrayList<JMenuItem> mis = plg.getMenuAction();
                if (mis != null) {
                    for (JMenuItem m : mis) {
                        if (m instanceof JMenu) {
                            itemsWithSubmenu.add(m);
                        } else if (m instanceof JCheckBoxMenuItem) {
                            itemsToggle.add(m);
                        } else {
                            itemsPress.add(m);
                        }
                    }
                }
            }
            if (wrapper.isQuickToggleEnabled()) {

                ExtensionEnableAction toggle = new ExtensionEnableAction(wrapper);
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(toggle);
                itemsToggle.add(jmi);

            }
        }

        boolean pre = false;
        for (JMenuItem jmi : itemsWithSubmenu) {
            if (!pre && getComponentCount() > 0) {
                addSeparator();
                pre = true;
            }

            add(jmi);
        }

        pre = false;
        for (JMenuItem jmi : itemsPress) {
            if (!pre && getComponentCount() > 0) {
                addSeparator();
                pre = true;
            }

            add(jmi);
        }

        pre = false;
        for (JMenuItem jmi : itemsToggle) {
            if (!pre && getComponentCount() > 0) {
                addSeparator();
                pre = true;
            }
            add(jmi);
        }
    }

    public void onUpdated() {
        System.out.println(1);
        IOEQ.add(new Runnable() {

            public void run() {
                removeAll();
                updateMenu();
            }

        }, true);
    }
}
