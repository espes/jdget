package org.appwork.app.launcher.parameterparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
    private  String[]                                          rawArguments;
    /**
     * The eventsenderobjekt is used to add Listenersupport to this class.
     */
    private final Eventsender<CommandSwitchListener, CommandSwitch> eventSender;
    private HashMap<String, CommandSwitch>                          map;

    public ParameterParser(final String[] args) {
        rawArguments = args;
        eventSender = new Eventsender<CommandSwitchListener, CommandSwitch>() {

            @Override
            protected void fireEvent(final CommandSwitchListener listener, final CommandSwitch event) {
                listener.executeCommandSwitch(event);

            }

        };
    }

    /**
     * @param string
     * @return
     */
    public CommandSwitch getCommandSwitch(final String string) {
        return map.get(string);
    }

    /**
     * @return the {@link ParameterParser#eventSender}
     * @see ParameterParser#eventSender
     */
    public Eventsender<CommandSwitchListener, CommandSwitch> getEventSender() {
        return eventSender;
    }

    /**
     * @return
     */
    public String[] getRawArguments() {
        // TODO Auto-generated method stub
        return rawArguments;
    }

    public void setRawArguments(final String[] rawArguments) {
        this.rawArguments = rawArguments;
    }

    /**
     * @param string
     * @return
     */
    public boolean hasCommandSwitch(final String string) {
        return map.containsKey(string);
    }

    public HashMap<String, CommandSwitch> getMap() {
        return map;
    }

    /**
     * parses the command row. and fires {@link CommandSwitch} for each switch
     * command
     * 
     * @param commandFilePath
     *            TODO
     */
    public void parse(final String commandFilePath) {

        map = new HashMap<String, CommandSwitch>();

        if (commandFilePath != null && Application.getResource(commandFilePath).exists()) {

            try {
                this.parse(ShellParser.splitCommandString(IO.readFileToString(Application.getResource(commandFilePath)).replaceAll("[\r\n]", " ")).toArray(new String[] {}));
            } catch (final IOException e) {
                Log.exception(e);
            }
        }

        this.parse(rawArguments);
    }

    /**
     * @param startArguments2
     */
    private void parse(final String[] startArguments) {

        String switchCommand = null;
        final java.util.List<String> params = new ArrayList<String>();
        for (String var : startArguments) {
            if (var.startsWith("-")) {
                while (var.length() > 0 && var.startsWith("-")) {
                    var = var.substring(1);
                }
                if (switchCommand != null || params.size() > 0) {
                    CommandSwitch cs;
                    if (switchCommand != null) {
                        switchCommand = switchCommand.toLowerCase(Locale.ENGLISH);
                    }
                    getEventSender().fireEvent(cs = new CommandSwitch(switchCommand, params.toArray(new String[] {})));
                    map.put(switchCommand, cs);
                }
                switchCommand = var;

                params.clear();
            } else {
                params.add(var);
            }
        }
        if (switchCommand != null || params.size() > 0) {
            CommandSwitch cs;
            if (switchCommand != null) {
                switchCommand = switchCommand.toLowerCase(Locale.ENGLISH);
            }
            getEventSender().fireEvent(cs = new CommandSwitch(switchCommand, params.toArray(new String[] {})));
            map.put(switchCommand, cs);
        }
    }

}
