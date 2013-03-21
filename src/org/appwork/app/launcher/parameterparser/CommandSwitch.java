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
public class CommandSwitch extends DefaultEvent {
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
    public CommandSwitch(final String switchCommand, final String[] array) {
        super(null);
        this.switchCommand = switchCommand;
        parameters = array;
    }

    /**
     * @return the {@link CommandSwitch#parameters}
     * @see CommandSwitch#parameters
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @return the {@link CommandSwitch#switchCommand}
     * @see CommandSwitch#switchCommand
     */
    public String getSwitchCommand() {
        return switchCommand;
    }

    /**
     * @param string
     * @return
     */
    public boolean hasParameter(final String string) {
       for(final String p:getParameters()){
           if(p.equals(string)) {
            return true;
        }
       }
        return false;
    }

}
