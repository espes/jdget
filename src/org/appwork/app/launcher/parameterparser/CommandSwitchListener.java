/**
 * 
 */
package org.appwork.app.launcher.parameterparser;

import java.util.EventListener;

/**
 * @author $Author: unknown$
 *
 */
public abstract class CommandSwitchListener implements EventListener {

    /**
     * @param event
     */
    abstract public void executeCommandSwitch(CommandSwitch event);

}
