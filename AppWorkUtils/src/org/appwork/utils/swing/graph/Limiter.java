/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.graph
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.graph;

import java.awt.Color;

/**
 * @author thomas
 * 
 */
public class Limiter {

    private int   value;
    private Color colorA;
    private Color colorB;

    /**
     * @param red
     */
    public Limiter(final Color a, final Color b) {
        this.colorA = a;
        this.colorB = b;
    }

    public Color getColorA() {
        return this.colorA;
    }

    public Color getColorB() {
        return this.colorB;
    }

    /**
     * @return
     */
    public int getValue() {

        return this.value;
    }

    public void setColorA(final Color colorA) {
        this.colorA = colorA;
    }

    public void setColorB(final Color colorB) {
        this.colorB = colorB;
    }

    public void setValue(final int value) {
        this.value = value;
    }

}
