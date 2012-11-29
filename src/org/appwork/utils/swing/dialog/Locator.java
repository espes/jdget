/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import org.appwork.utils.swing.dialog.TimerDialog.InternDialog;

/**
 * @author Thomas
 * 
 */
public interface Locator {

    /**
     * @param abstractDialog
     * @return
     */
    Point getLocationOnScreen(AbstractDialog<?> abstractDialog);

    void onClose(AbstractDialog<?> abstractDialog);

  
  


}
