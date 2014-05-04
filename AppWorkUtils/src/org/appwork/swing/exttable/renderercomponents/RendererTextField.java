/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable.renderercomponents
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.renderercomponents;

import java.awt.Rectangle;

import javax.swing.JTextField;

/**
 * @author Thomas
 * 
 */
public class RendererTextField extends JTextField {

    private boolean _visible=true;

    /**
     * Has to return false to avoid a drag&Drop cursor flicker bug <vr>
     * http://bugs.sun.com/view_bug.do?bug_id=6700748
     */
    @Override
    public boolean isVisible() {

        return _visible;
    }
    public void setVisible(boolean aFlag) {
        this._visible=aFlag;
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

    protected boolean _enabled = true;

    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this._enabled = enabled;
    }
}
