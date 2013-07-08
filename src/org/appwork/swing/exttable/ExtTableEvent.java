/**

 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import org.appwork.utils.event.SimpleEvent;

/**
 * @author thomas
 * @param <E>
 * 
 */
public class ExtTableEvent<P> extends SimpleEvent<ExtTable<?>, P, ExtTableEvent.Types> {
    public static enum Types {
        CONTEXTMENU,
        DOUBLECLICK,
        /**
         * java.util.List<?>
         */
        SELECTION_CHANGED,
        SHORTCUT_COPY,
        SHORTCUT_CUT,
        SHORTCUT_PASTE,
        SHORTCUT_DELETE,
        SHORTCUT_SEARCH,
        /**
         * MouseEvent
         */
        SORT_HEADER_CLICK, COLUMN_MODEL_UPDATE

    }

    /**
     * @param caller
     * @param type
     * @param parameters
     */
    public ExtTableEvent(final ExtTable<?> caller, final Types type, final P... parameters) {
        super(caller, type, parameters);
        // TODO Auto-generated constructor stub
    }

}
