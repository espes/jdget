/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.columns
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.border.Border;

import org.appwork.swing.MigPanel;

/**
 * @author thomas
 * 
 */
public class RendererMigPanel extends MigPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected Boolean         _enabled         = null;
    protected Boolean         _opaque          = null;

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public RendererMigPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);
    }

    @Override
    public boolean isEnabled() {
        if(_enabled==null) {
            return super.isEnabled();
        }
        return _enabled;
    }

    /**
     * Has to return false to avoid a drag&Drop cursor flicker bug <vr>
     * http://bugs.sun.com/view_bug.do?bug_id=6700748
     */
    @Override
    public boolean isVisible() {
        return false;
    }

    // /**
    // * * Overridden for performance reasons.
    // */
    // @Override
    // public void firePropertyChange(final String propertyName, final boolean
    // oldValue, final boolean newValue) {
    // /* we dont need propertychange events */
    // }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void repaint() {

    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Color bg = getBackground();
        if (isOpaque() && bg != null) {
            //Synthstyles paint a different background if panel is disabled.  We want the renderer to decide about the background
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            super.paintComponent(g);
        }

    }

    @Override
    public void paint(final Graphics g) {
        // TODO Auto-generated method stub
        super.paint(g);
    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void repaint(final long tm, final int x, final int y, final int width, final int height) {

    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void repaint(final Rectangle r) {

    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void revalidate() {

    }

    @Override
    public void setBackground(final Color bg) {
        if (bg != null && bg.equals(getBackground())) { return; }
        super.setBackground(bg);
        for (final Component c : getComponents()) {
            c.setBackground(bg);
        }

    }

    @Override
    public void setBorder(final Border border) {
        if (border == getBorder()) { return; }
        super.setBorder(border);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (_enabled != null && _enabled == enabled) { return; }
        _enabled = enabled;
        for (final Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }

    @Override
    public void setForeground(final Color fg) {
        if (fg != null && fg.equals(getForeground())) { return; }
        super.setForeground(fg);
        for (final Component c : getComponents()) {
            c.setForeground(fg);
        }
    }

    @Override
    public void setOpaque(final boolean isOpaque) {
        if (_opaque != null && _opaque == isOpaque) { return; }
        _opaque = isOpaque;
        super.setOpaque(isOpaque);
    }

}
