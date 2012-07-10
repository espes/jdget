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

/**
 * Requests a FileChooserDialog.
 * 
 * @param id
 *            ID of the dialog (used to save and restore the old directory)
 * @param title
 *            The Dialog's Window Title dialog-title or null for default
 * @param fileSelectionMode
 *            mode for selecting files (like JFileChooser.FILES_ONLY) or
 *            null for default
 * @param fileFilter
 *            filters the choosable files or null for default
 * @param multiSelection
 *            Multiple files choosable? or null for default
 * @param preSelection
 *            File which will be selected by default. leave null for
 *            automode
 * @return an array of files or null if the user cancel the dialog
 */
public enum FileChooserSelectionMode {
    FILES_ONLY(JFileChooser.FILES_ONLY),
    DIRECTORIES_ONLY(JFileChooser.DIRECTORIES_ONLY),
    FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES);
    private final int id;

    private FileChooserSelectionMode(final int num) {
        this.id = num;
    }

    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }
}