/**
 * 
 */
package org.appwork.app.launcher.parameterparser;

import org.appwork.utils.event.DefaultEvent;

/**
 * This event contains information about a startup parameter kombination like
 * -switch p1 p2 ...
 * 
 * @author $Author: unknown$
 * 
 * 
 */
public class SwitchEvent extends DefaultEvent {
    /**
     * the parameters that follow the {@link #switchCommand} without leading -
     */
    private final String[] parameters;

    /**
     * command. given at startup with --command or -command
     */
    private final String   switchCommand;

    /**
     * @param switchCommand
     * @param array
     */
    public SwitchEvent(final String switchCommand, final String[] array) {
        super(null);
        this.switchCommand = switchCommand;
        parameters = array;
    }

    /**
     * @return the {@link SwitchEvent#parameters}
     * @see SwitchEvent#parameters
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @return the {@link SwitchEvent#switchCommand}
     * @see SwitchEvent#switchCommand
     */
    public String getSwitchCommand() {
        return switchCommand;
    }

}
