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
import org.appwork.utils.locale._AWU;

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
        long abs = Math.abs(fileSize);
        final DecimalFormat c = new DecimalFormat("0.00");
        if (abs >= 1024 * 1024 * 1024 * 1024l) { return _AWU.T.literally_tebibyte(c.format(fileSize / (1024 * 1024 * 1024 * 1024.0))); }
        if (abs >= 1024 * 1024 * 1024l) { return _AWU.T.literally_gibibyte(c.format(fileSize / (1024 * 1024 * 1024.0))); }
        if (abs >= 1024 * 1024l) { return _AWU.T.literally_mebibyte(c.format(fileSize / (1024 * 1024.0))); }
        if (abs >= 1024l) { return _AWU.T.literally_kibibyte(c.format(fileSize / 1024.0)); }
        return _AWU.T.literally_byte(fileSize);
    }

    public static enum Unit {
        TB(1024l * 1024l * 1024l * 1024l),
        GB(1024l * 1024l * 1024l),
        MB(1024l * 1024l),
        KB(1024l),
        B(1l);
        private long bytes;

        private Unit(long bytes) {
            this.bytes = bytes;
        }

        public long getBytes() {
            return bytes;
        }
    }

    public static Unit getBestUnit(long fileSize) {
        long abs = Math.abs(fileSize);
        if (abs >= 1024 * 1024 * 1024 * 1024l) { return Unit.TB; }
        if (abs >= 1024 * 1024 * 1024l) { return Unit.GB; }
        if (abs >= 1024 * 1024l) { return Unit.MB; }
        if (abs >= 1024l) { return Unit.KB; }
        return Unit.B;
    }

    public static long getSize(final String string) {
        return SizeFormatter.getSize(string, true, false);
    }

    public static long getSize(String string, boolean kibi, boolean allowNegative) {
        boolean negative = false;
        if (allowNegative) {
            negative = Pattern.compile("\\D*\\-.*").matcher(string).matches();
        }
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
        if (Regex.matches(string, Pattern.compile("(tb|tbyte|tig|tib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit * unit * unit * unit;
        } else if (Regex.matches(string, Pattern.compile("(gb|gbyte|gig|gib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit * unit * unit;
        } else if (Regex.matches(string, Pattern.compile("(mb|mbyte|megabyte|mib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit * unit;
        } else if (Regex.matches(string, Pattern.compile("(kb|kbyte|kilobyte|kib)", Pattern.CASE_INSENSITIVE))) {
            res *= unit;
        }

        return negative ? -1 * Math.round(res) : Math.round(res);

    }

    public static long getSize(final String string, final boolean kibi) {
        return getSize(string, kibi, false);

    }
}
