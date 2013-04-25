/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.app.gui
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.app.gui;

import javax.swing.JDialog;

/**
 * @author Thomas
 * 
 */
public class ActiveDialogException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JDialog dialog;

    public JDialog getDialog() {
        return dialog;
    }

    /**
     * @param jDialog
     */
    public ActiveDialogException(final JDialog jDialog) {
        super("Active Dialog: "+jDialog);
        dialog = jDialog;
    }

}
