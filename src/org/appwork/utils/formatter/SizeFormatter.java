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

import org.appwork.utils.AwReg;

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
        if (fileSize < 0) fileSize = 0;
        DecimalFormat c = new DecimalFormat("0.00");
        if (fileSize >= (1024 * 1024 * 1024 * 1024l)) return c.format(fileSize / (1024 * 1024 * 1024 * 1024.0)) + " TiB";
        if (fileSize >= (1024 * 1024 * 1024l)) return c.format(fileSize / (1024 * 1024 * 1024.0)) + " GiB";
        if (fileSize >= (1024 * 1024l)) return c.format(fileSize / (1024 * 1024.0)) + " MiB";
        if (fileSize >= 1024l) return c.format(fileSize / 1024.0) + " KiB";
        return fileSize + " B";
    }

    public static long getSize(final String string) {
        String[][] matches = new AwReg(string, Pattern.compile("([\\d]+)[\\.|\\,|\\:]([\\d]+)", Pattern.CASE_INSENSITIVE)).getMatches();

        if (matches == null || matches.length == 0) {
            matches = new AwReg(string, Pattern.compile("([\\d]+)", Pattern.CASE_INSENSITIVE)).getMatches();
        }
        if (matches == null || matches.length == 0) { return -1; }

        double res = 0;
        if (matches[0].length == 1) {
            res = Double.parseDouble(matches[0][0]);
        }
        if (matches[0].length == 2) {
            res = Double.parseDouble(matches[0][0] + "." + matches[0][1]);
        }
        if (AwReg.matches(string, Pattern.compile("(gb|gbyte|gig|gib)", Pattern.CASE_INSENSITIVE))) {
            res *= 1024 * 1024 * 1024;
        } else if (AwReg.matches(string, Pattern.compile("(mb|mbyte|megabyte|mib)", Pattern.CASE_INSENSITIVE))) {
            res *= 1024 * 1024;
        } else if (AwReg.matches(string, Pattern.compile("(kb|kbyte|kilobyte|kib)", Pattern.CASE_INSENSITIVE))) {
            res *= 1024;
        }

        return Math.round(res);
    }
}
