/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;

import org.appwork.utils.swing.dialog.AbstractDialog;

/**
 * @author Thomas
 * 
 */
public class CenterOfScreenDialogLocator implements DialogLocator {

    private org.appwork.utils.swing.locator.CenterOfScreenLocator delegate;

    public CenterOfScreenDialogLocator() {
        delegate = new org.appwork.utils.swing.locator.CenterOfScreenLocator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#getLocationOnScreen(javax.swing
     * .JDialog)
     */
    @Override
    public Point getLocationOnScreen(AbstractDialog<?> d) {

        return delegate.getLocationOnScreen(d.getDialog());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.AbstractDialog)
     */
    @Override
    public void onClose(AbstractDialog<?> abstractDialog) {
        // TODO Auto-generated method stub

    }

}
