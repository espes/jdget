/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.renderer
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.renderer;

import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.appwork.utils.ImageProvider.ImageProvider;

/**
 * A Label for use in Renderers.
 * 
 * @author $Author: unknown$
 * 
 */
public class RenderLabel extends JLabel {

    /**
     * 
     */
    private static final long serialVersionUID = 1204940612879959884L;
    private boolean _enabled=true;
    private boolean _visible=true;

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        /* we dont need propertychange events */
    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void invalidate() {
    }

    // @Override
    // public boolean isVisible() {
    // return true;
    // }
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
     * * Overridden for performance reasons.
     */
    @Override
    public void setDisplayedMnemonicIndex(final int index) {
        /* we dont need mnemonic in a table */
    }

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    @Override
    public void setEnabled(final boolean b) {
        if(b==isEnabled())return;
       _enabled=b;
        if (!b && this.getIcon() != null) {
            this.setDisabledIcon(ImageProvider.getDisabledIcon(this.getIcon()));
        }
    }

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    @Override
    public void setIcon(final Icon icon) {
        if (icon == getIcon()) return;
        if (!this.isEnabled()) {
            this.setDisabledIcon(ImageProvider.getDisabledIcon(icon));
        }
        if (icon == null) {
            setDisabledIcon(null);
        }
        super.setIcon(icon);
    }

    public void setDisabledIcon(Icon disabledIcon) {
        if (disabledIcon == getDisabledIcon()) return;
        super.setDisabledIcon(disabledIcon);
    }

    @Override
    public void show(final boolean b) {

        // if (b) {
        // show();
        // } else {
        // hide();
        // }
        
    }
    public boolean isVisible() {
        return _visible;
    }
    public void setVisible(boolean aFlag) {
     
        _visible=aFlag;
    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }
}
