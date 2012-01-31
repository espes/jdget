/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

/**
 * @author Thomas
 *
 */
public interface ToolTipPainter {

    /**
     * @param tt
     * @return 
     */
    boolean showToolTip(ExtTooltip tt);

    /**
     * 
     */
    void hideTooltip();

}
