/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.trayicon
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.trayicon;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;

/**
 * @author thomas
 * 
 */
public class HoverEffect extends MouseAdapter {

    private final AbstractButton comp;

    public HoverEffect(final AbstractButton comp) {
        this.comp = comp;
    }

    @Override
    public void mouseEntered(final MouseEvent evt) {
        this.comp.setOpaque(true);
        this.comp.setContentAreaFilled(true);
        this.comp.setBorderPainted(true);
    }

    @Override
    public void mouseExited(final MouseEvent evt) {
        this.comp.setOpaque(false);
        this.comp.setContentAreaFilled(false);
        this.comp.setBorderPainted(false);
    }

}
