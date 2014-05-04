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

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

public class TitledSeparator extends JPanel {

    private static final long serialVersionUID = -8012696117008985390L;

    private JLabel            label;

    public TitledSeparator(final String title, final Color color) {
        super(new MigLayout("ins 0", "[][][grow,fill]", "[grow,fill]"));
        this.add(new JSeparator(), "gaptop 8,width 5!");
        this.add(this.label = new JLabel(title));
        if (color != null) {
            this.label.setForeground(color);
        }
        this.label.setFont(this.label.getFont().deriveFont(Font.BOLD));
        this.add(new JSeparator(), "gaptop 8");
        this.setOpaque(false);
    }

}
