package org.appwork.app.launcher.parameterparser;

import java.util.ArrayList;

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
    private String[] startArguments;
    /**
     * The eventsenderobjekt is used to add Listenersupport to this class.
     */
    private Eventsender<CommandSwitchListener, SwitchEvent> eventSender;

    public ParameterParser(String[] args) {
        this.startArguments = args;
        this.eventSender = new Eventsender<CommandSwitchListener, SwitchEvent>() {

            @Override
            protected void fireEvent(CommandSwitchListener listener, SwitchEvent event) {
                listener.executeCommandSwitch(event);

            }

        };
    }

    /**
     * @return the {@link ParameterParser#eventSender}
     * @see ParameterParser#eventSender
     */
    public Eventsender<CommandSwitchListener, SwitchEvent> getEventSender() {
        return eventSender;
    }

    /**
     * parses the command row. and fires {@link SwitchEvent} for each switch command
     */
    public void parse() {
        ArrayList<String> params = new ArrayList<String>();
        String switchCommand = null;

        for (String var : startArguments) {
            if (var.startsWith("-")) {
                while (var.length() > 0 && var.startsWith("-")) {
                    var = var.substring(1);
                }
                if (switchCommand != null) {
                    getEventSender().fireEvent(new SwitchEvent(switchCommand, params.toArray(new String[] {})));
                }
                switchCommand = var;
                params.clear();
            } else {
                params.add(var);
            }
        }
        if (switchCommand != null) {
            getEventSender().fireEvent(new SwitchEvent(switchCommand, params.toArray(new String[] {})));
        }

    }

}
