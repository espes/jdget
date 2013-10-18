package org.jdownloader.gui.views.linkgrabber;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;

import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuLink;
import org.jdownloader.extensions.ExtensionNotLoadedException;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.downloads.bottombar.SelfLayoutInterface;

public class LinkgrabberSearchMenuItem extends MenuItemData implements MenuLink, SelfLayoutInterface {
    public LinkgrabberSearchMenuItem() {
        super();
        setName(_GUI._.FilterMenuItem_FilterMenuItem());
        setIconKey(IconKey.ICON_SEARCH);
        setVisible(true);
        //
    }

    @Override
    public String getConstraints() {
        return "height 24!,aligny top,gapleft 2,pushx,growx";
    }

    public JComponent createItem(SelectionInfo<?, ?> selection) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, ExtensionNotLoadedException {

        return LinkgrabberSearchField.getInstance();
    }
}