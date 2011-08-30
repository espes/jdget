/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

import java.awt.Point;

import javax.swing.JWindow;

/**
 * @author thomas
 * 
 */
public abstract class ExtTooltip extends JWindow {

    /**
     * @param title
     */
    public ExtTooltip() {

        // this.setContentPane();
        final TooltipPanel con = this.createContent();
        this.add(con);

        this.pack();

    }

    /**
     * @return
     */
    public abstract TooltipPanel createContent();

    @Override
    public void dispose() {
        super.dispose();
        this.removeMouseListener(ToolTipController.getInstance());
    }

    /**
     * @param mousePosition
     */
    public void show(final Point mousePosition) {
        this.setLocation(mousePosition.x + 5, mousePosition.y + 5);
        this.pack();

        this.setVisible(true);

    }

}
