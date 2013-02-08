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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseListener;

import javax.swing.JToolTip;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.tooltips.config.ExtTooltipSettings;
import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public abstract class ExtTooltip extends JToolTip implements AncestorListener {

    /**
     * 
     */
    private static final long  serialVersionUID = -2212735987320956801L;

    /**
     * 
     */
    public static final String DEFAULT          = "default";

    /**
     * @param id
     * @return
     */
    public static ExtTooltipSettings createConfig(final String id) {

        return JsonConfig.create(Application.getResource("cfg/ExtTooltipSettings_" + id), ExtTooltipSettings.class);
    }

    /**
     * @param black
     */
    public static void setForgroundColor(final Color black) {
        ExtTooltip.createConfig(ExtTooltip.DEFAULT).setForegroundColor(black.getRGB());

    }

    private final ExtTooltipSettings config;

    protected MigPanel               panel;

    private int                      w    = 0;

    private int                      h    = 0;

    private long                     lastResize;

    private long                     lastResizeH;

    private final int                test = 0;

    /**
     * @param title
     */
    public ExtTooltip() {
        super();
        setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        config = ExtTooltip.createConfig(getID());

        panel = createContent();

        // this.add(con);
        setTipText("");

        // this.setUI(null);

        // this.setOpaque(true);
        // this.setBackground(null);

        if (panel != null) {
            this.add(panel);
        }
        addAncestorListener(this);
    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        /*
         * Make sure that we have only one listener
         */
        super.removeMouseListener(l);
        super.addMouseListener(l);
    }

    @Override
    public void ancestorAdded(final AncestorEvent event) {

        h = 0;
        w = 0;
        lastResize = 0;
        lastResizeH = 0;
    }

    @Override
    public void ancestorMoved(final AncestorEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ancestorRemoved(final AncestorEvent event) {

    }

    /**
     * @return
     */
    public abstract TooltipPanel createContent();

    @Override
    public Rectangle getBounds() {

        return super.getBounds();

    }

    @Override
    public Rectangle getBounds(final Rectangle rv) {
        return super.getBounds(rv);
    }

    public ExtTooltipSettings getConfig() {
        return config;
    }

    @Override
    public int getHeight() {
        if (panel == null) { return 0; }
        final Insets insets = this.getInsets();
        final int th = panel.getPreferredSize().height + insets.top + insets.bottom;
        if (th > h) {
            h = th;
            final Container parent = getParent();
            if (parent != null) {
                final Rectangle b = parent.getBounds();
                parent.setBounds(b.x, b.y, w, h);
            }

        } else if (th < h) {
            if (System.currentTimeMillis() - lastResizeH > 1000) {
                h -= (h - th) * (System.currentTimeMillis() - lastResizeH - 1000) / 10000;
                final Container parent = getParent();
                if (parent != null) {
                    final Rectangle b = parent.getBounds();
                    parent.setBounds(b.x, b.y, w, h);
                }
            }
        }

        return h;
    }

    /**
     * @return
     */
    protected String getID() {

        return ExtTooltip.DEFAULT;
    }

    @Override
    public Dimension getPreferredSize() {
        if (panel == null) { return new Dimension(0, 0); }
        final Dimension dim = panel.getPreferredSize();
        final Insets insets = this.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;

        return dim;
    }

    @Override
    public int getWidth() {
        if (panel == null) { return 0; }
        final Insets insets = this.getInsets();
        final int tw = panel.getPreferredSize().width + insets.left + insets.right;
        if (tw > w) {
            w = tw;
            final Container parent = getParent();
            if (parent != null) {
                final Rectangle b = parent.getBounds();
                parent.setBounds(b.x, b.y, w, h);
            }
            lastResize = System.currentTimeMillis();

        } else if (tw < w) {
            if (System.currentTimeMillis() - lastResize > 1000) {
                w -= (w - tw) * (System.currentTimeMillis() - lastResize - 1000) / 10000;
                final Container parent = getParent();
                if (parent != null) {
                    final Rectangle b = parent.getBounds();
                    parent.setBounds(b.x, b.y, w, h);
                }
            }
        }

        return w;

    }

    /**
     * normal behaviour is, that a new tooltip will be shown immediately if we
     * move the mouse to a new tooltip component within a short time. if this
     * method is false, this behaviour will not be active after this tooltip.
     * 
     * @return
     */
    public boolean isLastHiddenEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * 
     */
    public void onHide() {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    public void onShow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void paint(final Graphics g) {
        // this.getLayout().layoutContainer(this);
        if (panel != null) {
            panel.setSize(panel.getPreferredSize());
            final Insets insets = this.getInsets();
            panel.setLocation(insets.left, insets.top);
        }
        super.paint(g);

    }

    @Override
    protected void paintChildren(final Graphics g) {
        super.paintChildren(g);

    }

    @Override
    protected void paintComponent(final Graphics g) {

        super.paintComponent(g);

        // final Insets insets = this.getInsets();
        // g.translate(insets.left, insets.top);
        // this.panel.setSize(this.panel.getPreferredSize());
        // this.panel.repaint();
        // if (this.test++ < 5) {
        // this.panel.paintComponents(g);

        // }

    }

    @Override
    public void paintComponents(final Graphics g) {
        super.paintComponents(g);

    }

    /**
     * @return
     */
    abstract public String toText();
}
