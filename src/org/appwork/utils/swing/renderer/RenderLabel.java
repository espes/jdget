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
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.resources.AWUTheme;
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
    private boolean           _enabled         = true;
    private boolean           _visible         = true;

    private Icon              customDisabledIcon;

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
    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public boolean isVisible() {
        return _visible;
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
     * WIth this workaround, we avoid the disabled icon getting painted by the
     * look and feel.
     */
    public Icon getDisabledIcon() {
        if (customDisabledIcon == null) {          
            final Icon ico = getIcon();
            if (ico != null && ico instanceof ImageIcon) {
                AWUTheme.I().getDisabledIcon((ImageIcon) ico);
            } else {
                return ico;
            }
        }

        return customDisabledIcon;
    }

    @Override
    public void setDisabledIcon(final Icon disabledIcon) {
       
        if (disabledIcon == customDisabledIcon) { return; }
        // * WIth this workaround, we avoid the disabled icon getting painted by
        // the look and feel.
        customDisabledIcon = disabledIcon;
        super.setDisabledIcon(disabledIcon);
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
        if (b == isEnabled()) { return; }
        _enabled = b;
        if (!b && getIcon() != null) {
            setDisabledIcon(ImageProvider.getDisabledIcon(getIcon()));
        }
    }

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    @Override
    public void setIcon(final Icon icon) {
        if (icon == getIcon()) { return; }
        if (!isEnabled()) {
            setDisabledIcon(ImageProvider.getDisabledIcon(icon));
        }
        if (icon == null) {
            setDisabledIcon(null);
        }
        super.setIcon(icon);
    }

    @Override
    public void setText(final String text) {
        if (text == null && getText() == null) { return; }
        if (text != null && text.equals(getText())) { return; }
        super.setText(text);
    }

    @Override
    public void setVisible(final boolean aFlag) {
        _visible = aFlag;
    }

    @Override
    public void show(final boolean b) {

        // if (b) {
        // show();
        // } else {
        // hide();
        // }

    }

    /**
     * * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }
}
