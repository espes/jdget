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

    /**
     * * Overridden for performance reasons.
     */
    public void invalidate() {
    }

    /**
     * * Overridden for performance reasons.
     */
    public void validate() {
    }

    /**
     * * Overridden for performance reasons.
     */
    public void revalidate() {
    }

    /**
     * * Overridden for performance reasons.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * * Overridden for performance reasons.
     */
    public void repaint(Rectangle r) {
    }

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    public void setIcon(Icon icon) {
        if (!isEnabled()) {
            this.setDisabledIcon(ImageProvider.getDisabledIcon(icon));
        }
        super.setIcon(icon);
    }

    /**
     * for renderer reasons, there is a bug in java, that disabled icons to not
     * get cached properly. thats why we override the method here and extend it
     * to use a cached disabled icon
     */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (!b && getIcon() != null) {
            this.setDisabledIcon(ImageProvider.getDisabledIcon(getIcon()));
        }

    }

    /**
     * * Overridden for performance reasons.
     */
    public void repaint() {
    }
}
