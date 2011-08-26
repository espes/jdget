/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components;

import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * @author thomas
 * 
 */
public interface Badgeable {

    /**
     * @param migPanel
     * @param g
     */
    void paintBadge(BadgePainter painter, Graphics g);

    void setBadgeIcon(ImageIcon icon);
}
