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

package jd.gui.swing.jdgui.views.settings.sidebar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import jd.gui.swing.jdgui.views.settings.ConfigPanel;
import jd.gui.swing.jdgui.views.settings.panels.advanced.AdvancedSettings;
import jd.gui.swing.laf.LookAndFeelController;
import net.miginfocom.swing.MigLayout;

import org.appwork.utils.ColorUtils;
import org.jdownloader.gui.settings.AbstractConfigPanel;

public class TreeRenderer extends JPanel implements ListCellRenderer {

    private static final long          serialVersionUID = -3927390875702401200L;

    private static final Dimension     SMALL_DIMENSION  = new Dimension(0, 25);

    public static Dimension            DIMENSION        = new Dimension(0, 55);

    private final Font                 orgFont;
    private final Font                 boldFont;

    private JLabel                     lbl;

    private Color                      f;

    private Color                      alternateHighlight;

    private Color                      selectedBackground;

    private ExtensionPanelListRenderer extension;

    public TreeRenderer() {
        super(new MigLayout("ins 0 ,wrap 1", "[grow,fill]", "[]"));

        lbl = new JLabel();

        add(lbl, "");

        lbl.setVerticalTextPosition(JLabel.BOTTOM);
        lbl.setHorizontalTextPosition(JLabel.CENTER);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        orgFont = lbl.getFont();
        boldFont = lbl.getFont().deriveFont(Font.BOLD);

        // this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        f = lbl.getForeground();
        selectedBackground = ColorUtils.getAlphaInstance(f, 60);

        int c = LookAndFeelController.getInstance().getLAFOptions().getPanelHeaderColor();
        if (c >= 0) {
            selectedBackground = ColorUtils.getAlphaInstance((new Color(c)), 230);
        }

        alternateHighlight = ColorUtils.getAlphaInstance(lbl.getForeground(), 4);
        // a=NewTheme.I().getColor(ColorUtils.getAlphaInstance(lbl.getForeground(),
        // 4));

        lbl.setOpaque(false);
        setOpaque(false);
        setBackground(null);
        lbl.setBackground(null);
        extension = new ExtensionPanelListRenderer();
        // this.setPreferredSize(new Dimension(200, 60));
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setBorder(null);
        if (value == null) return this;
        if (value instanceof CheckBoxedEntry) {
            return extension.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        } else if (value instanceof AdvancedSettings) {
            AbstractConfigPanel te = (AbstractConfigPanel) value;
            setText(te.getTitle());
            setIcon(te.getIcon());
            lbl.setVerticalTextPosition(JLabel.CENTER);
            lbl.setHorizontalTextPosition(JLabel.RIGHT);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            setPreferredSize(SMALL_DIMENSION);
            this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, selectedBackground));

        } else if (value instanceof AbstractConfigPanel) {
            AbstractConfigPanel te = (AbstractConfigPanel) value;

            setText(te.getTitle());
            setIcon(te.getIcon());
            lbl.setVerticalTextPosition(JLabel.BOTTOM);
            lbl.setHorizontalTextPosition(JLabel.CENTER);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            setPreferredSize(TreeRenderer.DIMENSION);

        } else if (value instanceof ExtensionHeader) {
            return ((ExtensionHeader) value);
        } else {
            ConfigPanel te = (ConfigPanel) value;
            setText(te.getTitle());
            setIcon(te.getIcon());
            lbl.setVerticalTextPosition(JLabel.BOTTOM);
            lbl.setHorizontalTextPosition(JLabel.CENTER);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            setPreferredSize(TreeRenderer.DIMENSION);
        }

        if (isSelected) {
            lbl.setFont(boldFont);
            // lbl.setBorder(brd);
            setBackground(selectedBackground);
            // lbl.setForeground(b);
            setOpaque(true);
            // lbl.setForeground(b);

        } else {
            lbl.setFont(orgFont);
            if (index % 2 == 0) {
                setBackground(alternateHighlight);
                setOpaque(true);
            } else {
                setOpaque(false);
                setBackground(null);
            }
        }
        // label.setPreferredSize(new Dimension(200, 20));
        return this;
    }

    private void setIcon(Icon icon) {
        lbl.setIcon(icon);
    }

    private void setText(String name) {
        lbl.setText(name);
    }

    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void validate() {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // *
    // * @since 1.5
    // */
    // @Override
    // public void invalidate() {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // *
    // * @since 1.5
    // */
    // @Override
    // public void repaint() {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void revalidate() {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void repaint(long tm, int x, int y, int width, int height) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void repaint(Rectangle r) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // protected void firePropertyChange(String propertyName, Object oldValue,
    // Object newValue) {
    // // Strings get interned...
    // if (propertyName == "text" || ((propertyName == "font" || propertyName ==
    // "foreground") && oldValue != newValue &&
    // getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null))
    // {
    //
    // super.firePropertyChange(propertyName, oldValue, newValue);
    // }
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, byte oldValue, byte
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, char oldValue, char
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, short oldValue, short
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, int oldValue, int
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, long oldValue, long
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, float oldValue, float
    // newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, double oldValue,
    // double newValue) {
    // }
    //
    // /**
    // * Overridden for performance reasons. See the <a
    // * href="#override">Implementation Note</a> for more information.
    // */
    // @Override
    // public void firePropertyChange(String propertyName, boolean oldValue,
    // boolean newValue) {
    // }

}
