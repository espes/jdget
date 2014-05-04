package org.appwork.app.launcher.parameterparser;

import java.util.ArrayList;

import org.appwork.utils.locale._AWU;

/**
 * @author $Author: unknown$
 */
public abstract class CommandLineApp {
    /**
     * Switchcommands (-command)
     */
    private final String[]            commands;
    /**
     * Description of this commandline App. Is printed to stdout on help command
     */
    private String                    description;
    /**
     * Parameters required for this command. String[]{name,description}
     */
    private final java.util.List<String[]> parameters;

    public CommandLineApp(final String... commands) {
        this.commands = commands;
        parameters = new ArrayList<String[]>();
    }

    /**
     * adds a new Parameter to the app
     * 
     * @param name
     *            of the parameter
     * @param description
     *            of the parameter
     */
    public void addParameter(final String name, final String description) {
        parameters.add(new String[] { name, description });

    }

    /**
     * Executes the Apps Features
     * 
     * @param event
     */
    abstract public void execute(CommandSwitch event);

    /**
     * @return the {@link CommandLineApp#commands}
     * @see CommandLineApp#commands
     */
    public String[] getCommands() {
        return commands;
    }

    /**
     * @return the {@link CommandLineApp#description}
     * @see CommandLineApp#description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the {@link CommandLineApp#parameters}
     * @see CommandLineApp#parameters
     */
    public java.util.List<String[]> getParameters() {
        return parameters;
    }

    public void onEmptyCommand(final CommandSwitch event) {
    }

    /**
     * @param description
     *            the {@link CommandLineApp#description} to set
     * @see CommandLineApp#description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_AWU.T.COMMANDLINEAPP_COMMAND());
        for (int i = 0; i < commands.length; i++) {
            sb.append(commands[i]);
            if (i < commands.length - 1) {
                sb.append(" / ");
            }
        }
        sb.append(" | ").append(getDescription());
        sb.append("\r\n");
        for (final String[] parameter : parameters) {
            sb.append("    ").append(parameter[0]).append(" | ").append(parameter[1]).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

}
