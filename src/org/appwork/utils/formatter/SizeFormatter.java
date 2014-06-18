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
        final long abs = Math.abs(fileSize);
        final DecimalFormat c = new DecimalFormat("0.00");
        if (abs >= Unit.TB.getBytes1024()) { return _AWU.T.literally_tebibyte(c.format(fileSize / (double) Unit.TB.getBytes1024())); }
        if (abs >= Unit.GB.getBytes1024()) { return _AWU.T.literally_gibibyte(c.format(fileSize / (double) Unit.GB.getBytes1024())); }
        if (abs >= Unit.MB.getBytes1024()) { return _AWU.T.literally_mebibyte(c.format(fileSize / (double) Unit.MB.getBytes1024())); }
        if (abs >= Unit.KB.getBytes1024()) { return _AWU.T.literally_kibibyte(c.format(fileSize / (double) Unit.KB.getBytes1024())); }
        return _AWU.T.literally_byte(fileSize);
    }

    public static enum Unit {
        TB(1024l * 1024l * 1024l * 1024l, 1000l * 1000l * 1000l * 1000l),
        GB(1024l * 1024l * 1024l, 1000l * 1000l * 1000l),
        MB(1024l * 1024l, 1000l * 1000l),
        KB(1024l, 1000l),
        B(1l, 1l);
        private final long bytes;
        private final long kibytes;

        public final long getBytes1024() {
            return this.kibytes;
        }

        private Unit(long kibytes, long bytes) {
            this.bytes = bytes;
            this.kibytes = kibytes;

        }

        public final long getBytes1000() {
            return this.bytes;
        }
    }

    public static Unit getSmallerUnit(Unit unit) {
        return Unit.values()[Math.min(Unit.values().length - 1, unit.ordinal() + 1)];
    }

    public static Unit getBiggerUnit(Unit unit) {
        return Unit.values()[Math.max(0, unit.ordinal() - 1)];
    }

    public static Unit getBestUnit(long fileSize) {
        final long abs = Math.abs(fileSize);
        if (abs >= Unit.TB.getBytes1024()) { return Unit.TB; }
        if (abs >= Unit.GB.getBytes1024()) { return Unit.GB; }
        if (abs >= Unit.MB.getBytes1024()) { return Unit.MB; }
        if (abs >= Unit.KB.getBytes1024()) { return Unit.KB; }
        return Unit.B;
    }

    public static long getSize(final String string) {
        return SizeFormatter.getSize(string, true, false);
    }

    private static final Pattern DOUBLE = Pattern.compile("([\\d]+)[.,:]([\\d]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER = Pattern.compile("([\\d]+)", Pattern.CASE_INSENSITIVE);

    public static long getSize(String string, boolean kibi, boolean allowNegative) {
        final boolean negative;
        if (allowNegative) {
            negative = Pattern.compile("\\D*\\-.*").matcher(string).matches();
        } else {
            negative = false;
        }
        String[][] matches = new Regex(string, SizeFormatter.DOUBLE).getMatches();
        if (matches == null || matches.length == 0) {
            matches = new Regex(string, SizeFormatter.NUMBER).getMatches();
        }
        if (matches != null && matches.length >= 1) {
            final long unitLong = kibi ? SizeFormatter.getBestUnit(string).getBytes1024() : SizeFormatter.getBestUnit(string).getBytes1000();
            if (matches[0].length == 2 && Long.parseLong(matches[0][1]) > 0) {
                final double ret = Double.parseDouble(matches[0][0] + "." + matches[0][1]) * unitLong;
                return negative ? -Math.round(ret) : Math.round(ret);
            } else {
                final long ret = Long.parseLong(matches[0][0]) * unitLong;
                return negative ? -ret : ret;
            }
        }
        return -1;
    }

    private static final Pattern TB = Pattern.compile("(tb|tbyte|tig|tib)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GB = Pattern.compile("(gb|gbyte|gig|gib)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MB = Pattern.compile("(mb|mbyte|megabyte|mib)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KB = Pattern.compile("(kb|kbyte|kilobyte|kib)", Pattern.CASE_INSENSITIVE);

    private static Unit getBestUnit(String unitText) {
        if (new Regex(unitText, SizeFormatter.TB).matches()) { return Unit.TB; }
        if (new Regex(unitText, SizeFormatter.GB).matches()) { return Unit.GB; }
        if (new Regex(unitText, SizeFormatter.MB).matches()) { return Unit.MB; }
        if (new Regex(unitText, SizeFormatter.KB).matches()) { return Unit.KB; }
        return Unit.B;
    }

    public static long getSize(final String string, final boolean kibi) {
        return SizeFormatter.getSize(string, kibi, false);

    }
}
