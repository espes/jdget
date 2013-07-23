/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class ToolTipDelayer extends DelayedRunnable {

    private final long delay;

    /**
     * @param minDelayInMS
     */
    public ToolTipDelayer(final long minDelayInMS) {
        super(ToolTipController.EXECUTER, minDelayInMS);
        this.delay = minDelayInMS;
        // TODO ToolTipController.EXECUTER-generated constructor stub
    }

    @Override
    public void delayedrun() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ToolTipController.getInstance().showTooltip();

            }
        };

    }

    public long getDelay() {
        return this.delay;
    }

    @Override
    public String getID() {
        return "ToolTipDelayer";
    }

}
