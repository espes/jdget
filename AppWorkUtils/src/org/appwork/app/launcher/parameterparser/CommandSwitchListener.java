/**
 * 
 */
package org.appwork.app.launcher.parameterparser;

import java.util.EventListener;

/**
 * @author $Author: unknown$
 *
 */
public  interface CommandSwitchListener extends EventListener {

    /**
     * @param event
     */
     public void executeCommandSwitch(CommandSwitch event);

}
