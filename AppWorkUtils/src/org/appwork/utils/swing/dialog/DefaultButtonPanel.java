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

import javax.swing.AbstractAction;
import javax.swing.JButton;

import org.appwork.swing.MigPanel;

/**
 * @author Thomas
 * 
 */
public class DefaultButtonPanel extends MigPanel {

    /**
     * @param string
     * @param string2
     * @param string3
     */
    public DefaultButtonPanel(final String ins, final String columns, final String rows) {
        super(ins, columns, rows);
    }

    /**
     * @param okButton
     */
    public void addOKButton(final JButton okButton) {
        super.add(okButton, "alignx right,tag ok,sizegroup confirms");
    }

    /**
     * @param cancelButton
     */
    public void addCancelButton(final JButton cancelButton) {
        add(cancelButton, "alignx right,tag cancel,sizegroup confirms");

    }

    /**
     * @param a
     * @return
     */
    public JButton addAction(final AbstractAction a) {

        String tag = (String) a.getValue("tag");
        if (tag == null) {
            tag = "help";
        }
        JButton bt;
        add(bt = new JButton(a), "tag " + tag + ",sizegroup confirms");

        return bt;
    }

}
