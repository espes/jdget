/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.columns
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.renderer;

import org.appwork.app.gui.MigPanel;

/**
 * @author thomas
 * 
 */
public class RendererMigPanel extends MigPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public RendererMigPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);
        // TODO Auto-generated constructor stub
    }

    /**
     * Has to return false to avoid a drag&Drop cursor flicker bug
     */
    @Override
    public boolean isVisible() {
        return false;
    }

}
