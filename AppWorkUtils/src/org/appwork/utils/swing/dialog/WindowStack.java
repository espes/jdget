/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Window;
import java.util.ArrayList;

/**
 * @author Thomas
 * 
 */
public class WindowStack extends ArrayList<Window> {

    private Window root;

    public WindowStack(final Window root) {
        this.root=root;
        if(root!=null){
            add(root);
        }

    }

    /**
     * @param frame
     */
    public void reset(final Window frame) {
        clear();
        if (frame != null) {
            this.add(frame);
        }
    }

}
