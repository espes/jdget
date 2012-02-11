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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseListener;

import javax.swing.JToolTip;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.MigPanel;
import org.appwork.storage.config.JsonConfig;
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
    private static final long serialVersionUID = -2212735987320956801L;

    /**
     * 
     */
    public static final String DEFAULT = "default";

    /**
     * @param id
     * @return
     */
    public static ExtTooltipSettings createConfig(final String id) {

        return JsonConfig.create(Application.getResource("cfg/ExtTooltipSettings_" + id), ExtTooltipSettings.class);
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
        this.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        this.config = ExtTooltip.createConfig(this.getID());

        this.panel = this.createContent();

        // this.add(con);
        this.setTipText("");

        // this.setUI(null);

//        this.setOpaque(true);
//        this.setBackground(null);

        if (this.panel != null) {
            this.add(this.panel);
        }
        this.addAncestorListener(this);
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

        this.h = 0;
        this.w = 0;
        this.lastResize = 0;
        this.lastResizeH = 0;
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
        return this.config;
    }

    @Override
    public int getHeight() {
        final Insets insets = this.getInsets();
        final int th = this.panel.getPreferredSize().height + insets.top + insets.bottom;
        if (th > this.h) {
            this.h = th;
            final Rectangle b = this.getParent().getBounds();
            this.getParent().setBounds(b.x, b.y, this.w, this.h);

        } else if (th < this.h) {
            if (System.currentTimeMillis() - this.lastResizeH > 1000) {
                this.h -= (this.h - th) * (System.currentTimeMillis() - this.lastResizeH - 1000) / 10000;
                final Rectangle b = this.getParent().getBounds();
                this.getParent().setBounds(b.x, b.y, this.w, this.h);
            }
        }

        return this.h;
    }

    /**
     * @return
     */
    protected String getID() {

        return ExtTooltip.DEFAULT;
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension dim = this.panel.getPreferredSize();
        final Insets insets = this.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;

        return dim;
    }

    @Override
    public int getWidth() {
        final Insets insets = this.getInsets();
        final int tw = this.panel.getPreferredSize().width + insets.left + insets.right;
        if (tw > this.w) {
            this.w = tw;
            final Rectangle b = this.getParent().getBounds();
            this.getParent().setBounds(b.x, b.y, this.w, this.h);

            this.lastResize = System.currentTimeMillis();

        } else if (tw < this.w) {
            if (System.currentTimeMillis() - this.lastResize > 1000) {
                this.w -= (this.w - tw) * (System.currentTimeMillis() - this.lastResize - 1000) / 10000;
                final Rectangle b = this.getParent().getBounds();
                this.getParent().setBounds(b.x, b.y, this.w, this.h);
            }
        }

        return this.w;

    }

    @Override
    public void paint(final Graphics g) {
        // this.getLayout().layoutContainer(this);
        this.panel.setSize(this.panel.getPreferredSize());
        final Insets insets = this.getInsets();
        this.panel.setLocation(insets.left, insets.top);
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
     * @param black
     */
    public static void setForgroundColor(Color black) {
       createConfig(DEFAULT).setForegroundColor(black.getRGB());
        
    }

    /**
     * @return
     */
    abstract public String toText();
}
