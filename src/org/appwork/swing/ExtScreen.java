/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing;

import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * @author Thomas
 *
 */
public class ExtScreen {

    private Insets insets;
    private Rectangle bounds;

    /**
     * @param defaultConfiguration
     * @return
     */
    public static ExtScreen create(GraphicsConfiguration defaultConfiguration) {
       ExtScreen ret = new ExtScreen();
       
       ret.insets = Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
     ret.bounds=defaultConfiguration.getBounds();
//       bound.x += ins.left;
//       bound.width -= ins.left + ins.right;
//       bound.y += ins.top;
//       bound.height -= ins.top + ins.bottom;
        return ret;
    }

    /**
     * @return
     */
    public int getY() {
        // TODO Auto-generated method stub
        return bounds.y;
    }

    /**
     * @return
     */
    public int getHeight() {
        // TODO Auto-generated method stub
        return bounds.height;
    }

    /**
     * @return
     */
    public int getX() {
        // TODO Auto-generated method stub
        return bounds.x;
    }

    /**
     * @return
     */
    public int getWidth() {
        // TODO Auto-generated method stub
        return bounds.width;
    }

    /**
     * @return
     */
    public Insets getInsets() {
        // TODO Auto-generated method stub
        return insets;
    }

    /**
     * @return
     */
  

}
