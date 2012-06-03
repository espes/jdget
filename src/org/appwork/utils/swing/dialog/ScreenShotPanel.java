/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Graphics;
import java.awt.Image;

import org.appwork.swing.MigPanel;

/**
 * @author thomas
 * 
 */
public class ScreenShotPanel extends MigPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 8107727218118044828L;
    private Image screenshot;

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public ScreenShotPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);
        this.setOpaque(true);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (this.ui != null) {
            final Graphics scratchGraphics = g == null ? null : g.create();
            try {
                // ui.update(scratchGraphics, this);

                if (this.screenshot != null) {
                    g.drawImage(this.screenshot, 0, 0, null);
                }
                // g.setColor(Color.ORANGE);
                // g.fillRect(0, 0, this.getWidth(), this.getHeight());
                this.ui.paint(g, this);

            } finally {
                scratchGraphics.dispose();
            }
        }

    }

    /**
     * @param screenshot
     */
    public void setScreenShot(final Image screenshot) {
        this.screenshot = screenshot;

    }

}
