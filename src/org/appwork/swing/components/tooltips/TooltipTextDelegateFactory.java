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

import javax.swing.JComponent;

/**
 * @author thomas
 * 
 */
public class TooltipTextDelegateFactory implements TooltipFactory {

    private final JComponent component;

    /**
     * @param circledProgressBar
     */
    public TooltipTextDelegateFactory(final JComponent component) {
        this.component = component;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.TooltipFactory#createTooltip()
     */
    @Override
    public ExtTooltip createTooltip() {
        final String ttt = this.component.getToolTipText();
        if (ttt != null) {
            return new BasicExtTooltip(this.component);

        } else {
            return null;
        }
    }

}
