/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.multiprogressbar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.multiprogressbar;

import java.awt.Color;

/**
 * @author Thomas
 * 
 */
public class Range {

    private long  from;
    private Color color = Color.BLACK;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    private long to;

    public Range(long from, long to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }
public String toString(){
    return from+"->"+to;
}
    /**
     * @param i
     * @param j
     */
    public Range(long from, long to) {
        this.from = from;
        this.to = to;
        this.color = null;
    }
}
