/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

import java.util.ArrayList;

/**
 * @author thomas
 * 
 */
public class RSMParser {
    class Entry {

    }

    private final String           originalSource;
    private final ArrayList<Entry> entries;

    /**
     * @param source
     */
    public RSMParser(final String source) {
        originalSource = source;

        entries = new ArrayList<Entry>();
        final int i = 0;
        while (true) {
            // new Regex(source, f.getName() +
            // "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\, \\d+\\)[\\,\\;]").getMatch(-1);

            // if (line == null) {
            // line = new Regex(source, f.getName() +
            // "\\s*\\(\"[^\r^\n]*?(?<!\\\\)\"\\)[\\,\\;]").getMatch(-1);
            // }

        }
    }

    /**
     * @param line
     * @return
     */
    public String getComment(final String line) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param name
     * @return
     */
    public String getLine(final String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
