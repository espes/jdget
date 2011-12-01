package org.appwork.app.launcher.parameterparser;

import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.utils.event.Eventsender;

/**
 * This class is used to parse and evaluate Startparameters
 * 
 * @author $Author: unknown $
 * 
 */
public class ParameterParser {
    /**
     * Stores the Applications startParameters
     */
    private String[]                                          startArguments;
    /**
     * The eventsenderobjekt is used to add Listenersupport to this class.
     */
    private Eventsender<CommandSwitchListener, CommandSwitch> eventSender;
    private HashMap<String, CommandSwitch>                    map;

    public ParameterParser(String[] args) {
        this.startArguments = args;
        this.eventSender = new Eventsender<CommandSwitchListener, CommandSwitch>() {

            @Override
            protected void fireEvent(CommandSwitchListener listener, CommandSwitch event) {
                listener.executeCommandSwitch(event);

            }

        };
    }

    /**
     * @return the {@link ParameterParser#eventSender}
     * @see ParameterParser#eventSender
     */
    public Eventsender<CommandSwitchListener, CommandSwitch> getEventSender() {
        return eventSender;
    }

    /**
     * parses the command row. and fires {@link CommandSwitch} for each switch
     * command
     */
    public void parse() {
        ArrayList<String> params = new ArrayList<String>();
        String switchCommand = null;
        map = new HashMap<String, CommandSwitch>();
        for (String var : startArguments) {
            if (var.startsWith("-")) {
                while (var.length() > 0 && var.startsWith("-")) {
                    var = var.substring(1);
                }
                if (switchCommand != null) {
                    CommandSwitch cs;
                    getEventSender().fireEvent(cs = new CommandSwitch(switchCommand, params.toArray(new String[] {})));
                    map.put(switchCommand, cs);
                }
                switchCommand = var;

                params.clear();
            } else {
                params.add(var);
            }
        }
        if (switchCommand != null) {
            CommandSwitch cs;
            getEventSender().fireEvent(cs = new CommandSwitch(switchCommand, params.toArray(new String[] {})));
            map.put(switchCommand, cs);
        }

    }

    /**
     * @param string
     * @return
     */
    public CommandSwitch getCommandSwitch(String string) {

        return map.get(string);
    }

}
