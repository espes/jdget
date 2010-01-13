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

import javax.swing.JLabel;

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
     * * Overridden for performance reasons.
     */
    public void repaint() {
    }
}
