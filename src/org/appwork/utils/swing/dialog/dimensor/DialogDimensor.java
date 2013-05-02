/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.dimensor;

import java.awt.Dimension;

import org.appwork.utils.swing.dialog.AbstractDialog;

/**
 * @author Thomas
 * 
 */
public interface DialogDimensor {
    public Dimension getDimension(final AbstractDialog<?> dialog);
    public void onClose(final AbstractDialog<?> dialog);
}
