/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components;

import java.awt.event.MouseListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Thomas
 * 
 */
public class ExtSpinner extends JSpinner {

    /**
     * 
     */
    private static final long serialVersionUID = -885721913501063289L;

    /**
     * @param spinnerNumberModel
     * @param maximum 
     * @param minimium 
     */
    public ExtSpinner(SpinnerNumberModel spinnerNumberModel) {
        super(spinnerNumberModel);

    }

    public synchronized void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().addMouseListener(l);

    }

    @Override
    public Object getNextValue() { 
        return super.getNextValue();
    }

    /**
     * @return
     */
    public int getIntValue() {
        // TODO Auto-generated method stub
      
        return ((Number) getValue()).intValue();
    }
    public long getLongValue() {
        // TODO Auto-generated method stub

        return ((Number) getValue()).longValue();
    }

}
