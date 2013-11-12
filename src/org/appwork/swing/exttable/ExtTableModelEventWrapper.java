/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import javax.swing.event.TableModelEvent;

/**
 * @author Thomas
 * 
 */
public class ExtTableModelEventWrapper extends ExtTableModelEvent {

    /**
     * @param extTableModel
     * @param e
     */
    public ExtTableModelEventWrapper(final ExtTableModel<?> extTableModel, final TableModelEvent e) {
        super(extTableModel, ExtTableModelEvent.Type.NATIVE_EVENT, e);
    }

    /* (non-Javadoc)
     * @see org.appwork.swing.exttable.ExtTableModelEvent#fire(org.appwork.swing.exttable.ExtTableModelListener)
     */
    @Override
    public void fire(final ExtTableModelListener listener) {
       listener.onExtTableModelEvent(this);
        
    }

}
