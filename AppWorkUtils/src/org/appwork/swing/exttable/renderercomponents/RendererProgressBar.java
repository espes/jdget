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

import javax.swing.JProgressBar;

/**
 * @author Thomas
 * 
 */
public class RendererProgressBar extends JProgressBar {

    private static final long serialVersionUID = 1204940612879959884L;
    private boolean           _enabled         = true;
    private boolean           _visible         = true;

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        /* we dont need propertychange events */
        if ("indeterminate".equals(propertyName)) {
            // this is required to forward indeterminate changes to the ui. This
            // would cfreate nullpointers in the uis because progresbar might
            // try to paint indeterminate states, but the ui has not been
            // initialized due to the missing event
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void invalidate() {
    }

    public boolean isEnabled() {
        return _enabled;
    }

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

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    @Override
    public void setEnabled(final boolean b) {
        _enabled = b;
    }

    public boolean isVisible() {
        return _visible;
    }

    public void setVisible(boolean aFlag) {

        _visible = aFlag;
    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }
}
