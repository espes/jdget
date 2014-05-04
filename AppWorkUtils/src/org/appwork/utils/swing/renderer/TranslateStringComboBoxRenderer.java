package org.appwork.utils.swing.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.appwork.utils.locale.Translate;

public class TranslateStringComboBoxRenderer extends BasicComboBoxRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 101050591804810792L;

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {

        return super.getListCellRendererComponent(list, ((Translate) value).s(), index, isSelected, cellHasFocus);
    }
}
