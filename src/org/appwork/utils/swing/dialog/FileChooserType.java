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

import javax.swing.JFileChooser;

public enum FileChooserType {
    OPEN_DIALOG(JFileChooser.OPEN_DIALOG),
    SAVE_DIALOG(JFileChooser.SAVE_DIALOG),
    CUSTOM_DIALOG(JFileChooser.CUSTOM_DIALOG),
    OPEN_DIALOG_WITH_PRESELECTION(JFileChooser.OPEN_DIALOG);
    private final int id;

    private FileChooserType(final int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }
}