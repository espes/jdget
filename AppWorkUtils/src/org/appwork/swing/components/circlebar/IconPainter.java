/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.circlebar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.circlebar;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * @author thomas
 * 
 */
public interface IconPainter {

    /**
     * @param bar
     *            TODO
     * @param g2
     * @param shape TODO
     * @param diameter
     * @param progress
     *            TODO
     */
    void paint(CircledProgressBar bar, Graphics2D g2, Shape shape, int diameter, double progress);

    /**
     * @return
     */
    Dimension getPreferredSize();

}
