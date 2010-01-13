/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

public class TitledSeparator extends JPanel {

    private static final long serialVersionUID = -8012696117008985390L;

    private JLabel label;

    public TitledSeparator(final String title) {
        super(new MigLayout("ins 0", "[][][grow,fill]", "[grow,fill]"));
        add(new JSeparator(), "gaptop 8,width 5!");
        add(label = new JLabel(title));
        label.setForeground(getBackground().darker().darker());
        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
        add(new JSeparator(), "gaptop 8");
        this.setOpaque(false);
    }

}
