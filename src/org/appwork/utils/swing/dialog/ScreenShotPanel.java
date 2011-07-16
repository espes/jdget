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

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JPanel;

import org.appwork.screenshot.ScreensShotHelper;

/**
 * @author thomas
 * 
 */
public class ScreenShotPanel extends JPanel {

    private Image fullShot;

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public ScreenShotPanel() {
        super();

        try {
            this.fullShot = ScreensShotHelper.getFullScreenShot();
        } catch (final AWTException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        this.setOpaque(true);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;

        if (this.ui != null) {
            final Graphics scratchGraphics = g == null ? null : g.create();
            try {
                // ui.update(scratchGraphics, this);

                final Point loc = this.getLocationOnScreen();

                g.drawImage(this.fullShot, 0, 0, this.getWidth(), this.getHeight(), loc.x, loc.y, loc.x + this.getWidth(), loc.y + this.getHeight(), null);

                // g.setColor(Color.ORANGE);
                // g.fillRect(0, 0, this.getWidth(), this.getHeight());
                this.ui.paint(g, this);

            } finally {
                scratchGraphics.dispose();
            }
        }

    }

}
