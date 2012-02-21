/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.multiprogressbar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.multiprogressbar;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.appwork.utils.ColorUtils;

/**
 * @author Thomas
 * 
 */
public class BasicMultiProgressModelUI extends MultiProgressBarUI implements MultiProgressListener {

    public BasicMultiProgressModelUI() {
        super();

    }

    public static ComponentUI createUI(final JComponent x) {
        return new BasicMultiProgressModelUI();
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
        Graphics2D g2 = (Graphics2D) g;
        MultiProgressBar bar = (MultiProgressBar) c;
        Range[] ranges = bar.getModel().getRanges();
        long max = bar.getModel().getMaximum();
        if (max == 0) return;
        if (ranges == null || ranges.length == 0) return;
        int w = bar.getWidth();
        int h = bar.getHeight();
        int to = 0;
        int from = 0;
        for (Range r : ranges) {

            Color color = r.getColor() != null ? r.getColor() : bar.getForeground();

            g2.setPaint(new GradientPaint(w / 2, h, color, w / 2, 0, ColorUtils.getAlphaInstance(color, 40)));

            to = (int) (((r.getTo() - r.getFrom()) * w) / max)+1;
            from = (int) ((r.getFrom() * w) / max);
            g2.fillRect(from, 0, to, h);
            g2.setColor(color);
            g2.drawLine(from, 0, from, h);
        }

    }

    private MultiProgressBar bar;

    private void installListeners() {
        bar.getEventSender().addListener(this);

    }

    @Override
    public void installUI(final JComponent c) {
        this.bar = (MultiProgressBar) c;
        this.installListeners();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.multiprogressbar.MultiProgressListener#onChanged
     * ()
     */
    @Override
    public void onChanged() {
        bar.repaint();
    }
}
