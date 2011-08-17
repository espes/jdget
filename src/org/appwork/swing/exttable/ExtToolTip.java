/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import javax.swing.JToolTip;

/**
 * @author thomas
 * 
 */
public class ExtToolTip extends JToolTip {
    public ExtToolTip() {
        super();
    }

    // @Override
    // public String getTipText() {
    // // TODO Auto-generated method stub
    // return "super.getTipText()" + this;
    // }

    /**
     * @param txt
     */
    public void setExtText(final String txt) {
        super.setTipText(txt);
    }

    @Override
    public void setTipText(final String tipText) {

    }

}
