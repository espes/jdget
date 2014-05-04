/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.parser
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.parser;

import java.util.ArrayList;

import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class ShellParser {

    private static int min(int space, int q, int dq) {
        if (space == -1) {
            space = Integer.MAX_VALUE;
        }
        if (q == -1) {
            q = Integer.MAX_VALUE;
        }
        if (dq == -1) {
            dq = Integer.MAX_VALUE;
        }

        return Math.min(Math.min(space, q), dq);
    }

    /**
     * Splits a Commandstring it its single commands <br>
     * <code>java -jar
     * ghd\"dfs "bjhn\"bdsa hgf" 'bn\"la' "" ' \\' 'bla'<br>
     * "java"<br>
     * "-jar"<br>
     * "ghd\"dfs"<br>
     * "bjhn\"bdsa hgf"<br>
     * "bn\"la"<br>
     * ""<br>
     * "  \\"<br>
     * "bla"<br>
     * </code>
     * @param command
     * @return
     */
   
    public static java.util.List<String> splitCommandString(String command) {
        final java.util.List<String> ret = new ArrayList<String>();

        while (true) {
            final int space = command.indexOf(" ");
            int q = command.indexOf("'");
            while (true) {
                if (q == -1) {
                    break;
                }
                int escapes = 0;
                int ec = 1;
                while (q - ec >= 0 && command.charAt(q - ec++) == '\\') {
                    escapes++;
                }
                if (escapes % 2 == 0) {
                    break;
                }
                q = command.indexOf("'", q + 1);
            }

            int dq = command.indexOf("\"");

            while (true) {
                if (dq == -1) {
                    break;
                }
                int escapes = 0;
                int ec = 1;
                while (dq - ec >= 0 && command.charAt(dq - ec++) == '\\') {

                    escapes++;
                }
                if (escapes % 2 == 0) {
                    break;
                }
                dq = command.indexOf("\"", dq + 1);
            }
            final int min = ShellParser.min(space, q, dq);
            if (min == Integer.MAX_VALUE) {
                if (command.trim().length() > 0) {
                    ret.add(command);
                }
                return ret;
            } else {
                if (min == space) {
                    final String p = command.substring(0, min).trim();
                    if (p.length() > 0) {
                        ret.add(p);
                    }
                    command = command.substring(min + 1);

                } else if (min == q) {
                    int nq = command.indexOf("'", min + 1);
                    while (true) {
                        if (nq == -1) {
                            nq = command.length() - 1;
                            Log.L.warning("Malformed commandstring");
                            break;
                        }
                        int escapes = 0;
                        int ec = 1;
                        while (command.charAt(nq - ec++) == '\\') {
                            escapes++;
                        }
                        if (escapes % 2 == 0) {
                            break;
                        }
                        nq = command.indexOf("'", nq + 1);
                    }
                    ret.add(command.substring(min + 1, nq));
                    command = command.substring(Math.min(nq + 2, command.length()));
                } else if (min == dq) {
                    int nq = command.indexOf("\"", min + 1);
                    while (true) {
                        if (nq == -1) {
                            nq = command.length() - 1;
                            Log.L.warning("Malformed commandstring");
                            break;
                        }
                        int escapes = 0;
                        int ec = 1;
                        while (command.charAt(nq - ec++) == '\\') {
                            escapes++;
                        }
                        if (escapes % 2 == 0) {
                            break;
                        }
                        nq = command.indexOf("\"", nq + 1);
                    }

                    ret.add(command.substring(min + 1, nq));
                    command = command.substring(Math.min(nq + 2, command.length()));
                }
            }

        }
    }
}
