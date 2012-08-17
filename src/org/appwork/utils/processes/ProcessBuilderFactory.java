package org.appwork.utils.processes;

import java.util.ArrayList;

import org.appwork.utils.os.CrossSystem;

public class ProcessBuilderFactory {

    public static ProcessBuilder create(String ... tiny) {

        return new ProcessBuilder(escape(tiny));
    }

    private static String[] escape(String[] tiny) {
        if (!CrossSystem.isWindows()) return tiny;

        // The windows processbuilder throws exceptions if a arguments starts with ", but does not end with " or vice versa
        String[] ret = new String[tiny.length];
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
    }

    public static ProcessBuilder create(java.util.List<String> splitCommandString) {
        return create(splitCommandString.toArray(new String[] {}));

    }
}
