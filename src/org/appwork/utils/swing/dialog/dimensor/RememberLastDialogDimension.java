/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog.dimensor
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.dimensor;

import java.awt.Dimension;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dimensor.RememberLastDimensor;

/**
 * @author Thomas
 * 
 */
public class RememberLastDialogDimension implements DialogDimensor {

    private String               id;
    private RememberLastDimensor delegate;

    /**
     * @param string
     */
    public RememberLastDialogDimension(final String id) {
        delegate = new RememberLastDimensor(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.dimensor.DialogDimensor#getPreferredSize
     * (org.appwork.utils.swing.dialog.AbstractDialog)
     */
    @Override
    public Dimension getDimension(final AbstractDialog<?> dialog) {
        // TODO Auto-generated method stub
        return delegate.getDimension(dialog.getDialog());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.dimensor.DialogDimensor#onClose(org.appwork
     * .utils.swing.dialog.AbstractDialog)
     */
    @Override
    public void onClose(final AbstractDialog<?> dialog) {
        delegate.onClose(dialog.getDialog());

    }

}
