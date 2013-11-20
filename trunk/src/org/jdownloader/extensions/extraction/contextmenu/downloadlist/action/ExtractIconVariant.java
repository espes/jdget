package org.jdownloader.extensions.extraction.contextmenu.downloadlist.action;

import java.awt.AlphaComposite;

import org.appwork.swing.components.ExtMergedIcon;
import org.jdownloader.images.AbstractIcon;

public class ExtractIconVariant extends ExtMergedIcon {

    private AbstractIcon mainIcon;

    public ExtractIconVariant(String badge, int size) {
        this(badge, size, (size * 2) / 3);
    }

    public ExtractIconVariant(String badge, int size, int badgesize) {
        this(badge, size, badgesize, 0, 0);

    }

    public ExtractIconVariant(String badge, int size, int badgesize, int xOffset, int yOffset) {
        mainIcon = new AbstractIcon(org.jdownloader.gui.IconKey.ICON_COMPRESS, size);
        add(mainIcon, 0, 0, 0, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        AbstractIcon badgeIcon = new AbstractIcon(badge, badgesize);
        add(badgeIcon, xOffset + mainIcon.getIconWidth() - badgeIcon.getIconWidth(), yOffset + mainIcon.getIconHeight() - badgeIcon.getIconHeight());
    }

    public ExtMergedIcon crop() {
        return crop(mainIcon.getIconWidth(), mainIcon.getIconHeight());
    }

}
