package org.appwork.app.launcher.parameterparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.event.Eventsender;
import org.appwork.utils.logging.Log;
import org.appwork.utils.parser.ShellParser;

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
     * 
     * @param commandFilePath
     *            TODO
     */
    public void parse(String commandFilePath) {

        map = new HashMap<String, CommandSwitch>();

        if (commandFilePath != null && Application.getResource(commandFilePath).exists()) {

            try {
                parse(ShellParser.splitCommandString(IO.readFileToString(Application.getResource(commandFilePath)).replaceAll("[\r\n]", " ")).toArray(new String[] {}));
            } catch (IOException e) {
                Log.exception(e);
            }
        }

        parse(startArguments);
    }

    /**
     * @param startArguments2
     */
    private void parse(String[] startArguments) {

        String switchCommand = null;
        ArrayList<String> params = new ArrayList<String>();
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

    /**
     * @param string
     * @return
     */
    public boolean hasCommandSwitch(String string) {

        return map.containsKey(string);
    }

    /**
     * @return
     */
    public String[] getRawArguments() {
        // TODO Auto-generated method stub
        return startArguments;
    }

}
