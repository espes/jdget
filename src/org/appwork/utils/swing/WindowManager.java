/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.Window;

/**
 * @author Thomas
 *
 */
public interface WindowManager {

    /**
     * @param w
     */
    void toFront(Window w,boolean requestFocus);
    
    void setVisible(Window w,boolean visible,boolean requestFocus,boolean forceToFront);

}
