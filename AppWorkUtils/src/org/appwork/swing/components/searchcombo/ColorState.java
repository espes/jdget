/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.searchcombo
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.searchcombo;

import java.awt.Color;

/**
 * @author thomas
 * 
 */
public class ColorState {

    private Color foreground;

    public ColorState(final Color foreground) {
        this.foreground = foreground;
    }

    public Color getForeground() {
        return this.foreground;
    }

    public void setForeground(final Color foreground) {
        this.foreground = foreground;
    }

    @Override
    public String toString() {
        return this.foreground + "";
    }

}
