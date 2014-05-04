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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

/**
 * A Buttonhandler to show the buttons background on mouseover only
 * 
 * @author $Author: unknown$
 */
public class MouseOverBackgroundListener extends MouseAdapter {
    private JButton button;
    private Border border;
    private Border borderb;

    public MouseOverBackgroundListener(JButton button) {
        if (button == null) throw new IllegalArgumentException("JButton parameter is null");
        this.button = button;
        border = BorderFactory.createEmptyBorder(2, 2, 0, 0);
        borderb = BorderFactory.createEmptyBorder(0, 0, 2, 2);
        button.addMouseListener(this);

        button.setContentAreaFilled(false);
        button.setBorder(null);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

        if (button.isEnabled()) button.setBorder(border);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (button.isEnabled()) button.setBorder(borderb);
    }
}
