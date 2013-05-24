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
        return this._enabled;
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
        if (bg != null && bg.equals(this.getBackground())) { return; }
        super.setBackground(bg);
        for (final Component c : this.getComponents()) {
            c.setBackground(bg);
        }

    }

    @Override
    public void setBorder(final Border border) {
        if (border == this.getBorder()) { return; }
        super.setBorder(border);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (this._enabled != null && this._enabled == enabled) { return; }
        this._enabled = enabled;
        for (final Component c : this.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    @Override
    public void setForeground(final Color fg) {
        if (fg != null && fg.equals(this.getForeground())) { return; }
        super.setForeground(fg);
        for (final Component c : this.getComponents()) {
            c.setForeground(fg);
        }
    }

    @Override
    public void setOpaque(final boolean isOpaque) {
        if (this._opaque != null && this._opaque == isOpaque) { return; }
        this._opaque = isOpaque;
        super.setOpaque(isOpaque);
    }

}
