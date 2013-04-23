package org.appwork.utils.processes;

import org.appwork.utils.os.CrossSystem;

public class ProcessBuilderFactory {

    public static ProcessBuilder create(final java.util.List<String> splitCommandString) {
        return ProcessBuilderFactory.create(splitCommandString.toArray(new String[] {}));

    }

    public static ProcessBuilder create(final String... tiny) {

        return new ProcessBuilder(ProcessBuilderFactory.escape(tiny));
    }

    private static String[] escape(final String[] tiny) {
        if (CrossSystem.isWindows() || CrossSystem.isOS2()) {
            // The windows processbuilder throws exceptions if a arguments
            // starts
            // with ", but does not end with " or vice versa
            final String[] ret = new String[tiny.length];
            //
            for (int i = 0; i < ret.length; i++) {
                if (tiny[i].startsWith("\"") && !tiny[i].endsWith("\"")) {
                    ret[i] = "\"" + tiny[i].replace("\"", "\\\"") + "\"";
                } else if (!tiny[i].startsWith("\"") && tiny[i].endsWith("\"")) {
                    ret[i] = "\"" + tiny[i].replace("\"", "\\\"") + "\"";
                } else {
                    ret[i] = tiny[i];
                }
            }

            return ret;
        } else {
            return tiny;
        }
    }
}
