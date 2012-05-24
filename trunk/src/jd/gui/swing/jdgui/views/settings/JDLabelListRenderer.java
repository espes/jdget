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

package jd.gui.swing.jdgui.views.settings;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import jd.gui.swing.components.JDLabelContainer;

/**
 * Cellrenderer für Copmboboxen mit Bildern
 * 
 * @author coalado
 */
public class JDLabelListRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 3607383089555373774L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value != null) {
            label.setText(((JDLabelContainer) value).getLabel());
            label.setIcon(((JDLabelContainer) value).getIcon());
        }
        setOpaque(false);
        putClientProperty("Synthetica.opaque", Boolean.FALSE);
        return label;
    }
}
