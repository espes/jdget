/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.formatter
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.formatter;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.appwork.utils.Regex;

/**
 * @author $Author: unknown$
 * 
 */
public class SizeFormatter {

    /**
     * Formats filesize from Bytes to the best readable form
     * 
     * @param fileSize
     *            in Bytes
     * @return
     */
    public static String formatBytes(long fileSize) {
        if (fileSize < 0) {
            fileSize = 0;
        }
        final DecimalFormat c = new DecimalFormat("0.00");
        if (fileSize >= 1024 * 1024 * 1024 * 1024l) { return c.format(fileSize / (1024 * 1024 * 1024 * 1024.0)) + " TiB"; }
        if (fileSize >= 1024 * 1024 * 1024l) { return c.format(fileSize / (1024 * 1024 * 1024.0)) + " GiB"; }
        if (fileSize >= 1024 * 1024l) { return c.format(fileSize / (1024 * 1024.0)) + " MiB"; }
        if (fileSize >= 1024l) { return c.format(fileSize / 1024.0) + " KiB"; }
        return fileSize + " B";
    }

    public static long getSize(final String string) {
        return SizeFormatter.getSize(string, true);
    }

    public static long getSize(final String string, final boolean kibi) {
        int unit = 1000;
        if (kibi) {
            unit = 1024;
        }
        String[][] matches = new Regex(string, Pattern.compile("([\\d]+)[\\.|\\,|\\:]([\\d]+)", Pattern.CASE_INSENSITIVE)).getMatches();

        if (matches == null || matches.length == 0) {
            matches = new Regex(string, Pattern.compile("([\\d]+)", Pattern.CASE_INSENSITIVE)).getMatches();
        }
        if (matches == null || matches.length == 0) { return -1; }

        double res = 0;
        if (matches[0].length == 1) {
            res = Double.parseDouble(matches[0][0]);
        }
        if (matches[0].length == 2) {
            res = Double.parseDouble(matches[0][0] + "." + matches[0][1]);
        }
        if (Regex.matches(string, Pattern.compile("(gb|gbyte|gig|gib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit * unit * unit;
        } else if (Regex.matches(string, Pattern.compile("(mb|mbyte|megabyte|mib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit * unit;
        } else if (Regex.matches(string, Pattern.compile("(kb|kbyte|kilobyte|kib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit;
        }

        return Math.round(res);
    }
}
