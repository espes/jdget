/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author thomas
 * 
 */
public interface ToolTipHandler {

    /**
     * @param toolTipManager
     */
    void addMouseListener(MouseListener component);

    void addMouseMotionListener(MouseMotionListener component);

    /**
     * @param mousePosition
     * @return
     */
    ExtTooltip createExtTooltip(Point mousePosition);

    /**
     * @return true if a component'S tooltip should not be shown again after
     *         hide until the user moves mouse away and then over component
     *         again.
     * 
     */
    boolean isTooltipDisabledUntilNextRefocus();

    /**
     * @param toolTipController
     */
    void removeMouseListener(MouseListener toolTipController);

    /**
     * @param toolTipController
     */
    void removeMouseMotionListener(MouseMotionListener toolTipController);

    /**
     * 
     * 
     * 
     * @return true if the tooltip should be updated
     */
    boolean updateTooltip(ExtTooltip activeToolTip, MouseEvent e);

    /**
     * @return true if component can show a tooltip without having the current focus
     */
    boolean isTooltipWithoutFocusEnabled();

    /**
     * @param mousePositionOnScreen TODO
     * @return
     */
    int getTooltipDelay(Point mousePositionOnScreen);

}
