/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.renderer
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.renderer;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 * @author thomas
 * 
 */
public class RendererComboBoxModel implements ComboBoxModel {

    private Object selection;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener
     * )
     */
    @Override
    public void addListDataListener(final ListDataListener l) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListModel#getElementAt(int)
     */
    @Override
    public Object getElementAt(final int index) {
        // TODO Auto-generated method stub
        return this.selection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    @Override
    public Object getSelectedItem() {
        // TODO Auto-generated method stub
        return this.selection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListModel#getSize()
     */
    @Override
    public int getSize() {
        // TODO Auto-generated method stub
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.
     * ListDataListener)
     */
    @Override
    public void removeListDataListener(final ListDataListener l) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    @Override
    public void setSelectedItem(final Object anItem) {
        this.selection = anItem;

    }

}
