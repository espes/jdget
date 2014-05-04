package org.appwork.utils.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public abstract class ImageListRenderer<T> extends JLabel implements ListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param value
     * @return
     */
    public abstract Icon getImage(T value);

    @SuppressWarnings("unchecked")

    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        setText(getText((T) value));
        setIcon(getImage((T) value));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }

    /**
     * @param value
     * @return
     */
    public abstract String getText(T value);

}
