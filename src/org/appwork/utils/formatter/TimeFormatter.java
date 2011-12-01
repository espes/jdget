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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.Regex;

public class TimeFormatter {

    private static final String[] dateformats  = new String[] { "EEE, dd-MMM-yy HH:mm:ss z", "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd MMM yyyy HH:mm:ss z", "EEE MMM dd HH:mm:ss z yyyy", "EEE, dd-MMM-yyyy HH:mm:ss z", "EEEE, dd-MMM-yy HH:mm:ss z" };

    public static final int       HIDE_SECONDS = 1 << 1;
    public static final int       HIDE_MARKER  = 1 << 2;
    public static final int       CLOCK        = 1 << 3;

    public static String formatMilliSeconds(final long totalSeconds, final int flags) {
        return TimeFormatter.formatSeconds(totalSeconds / 1000, flags);
    }

    public static String formatSeconds(long totalSeconds, final int flags) {
        long days, hours, minutes, seconds;
        final StringBuilder string = new StringBuilder();

        days = totalSeconds / (24 * 60 * 60);
        totalSeconds -= days * 24 * 60 * 60;
        hours = totalSeconds / (60 * 60);
        totalSeconds -= hours * 60 * 60;
        minutes = totalSeconds / 60;
        seconds = totalSeconds - minutes * 60;

        if (!BinaryLogic.containsAll(flags, TimeFormatter.CLOCK)) {
            /* show days as extra field */
            if (days != 0) {
                string.append(days);
                string.append('d');
            }
        } else {
            /* add days to hours field */
            if (days != 0) {
                hours += days * 24;
            }
        }
        if (hours != 0 || string.length() != 0 || BinaryLogic.containsAll(flags, TimeFormatter.CLOCK)) {
            if (string.length() != 0) {
                string.append(':');
            }
            string.append(hours);
            if (BinaryLogic.containsNone(flags, TimeFormatter.HIDE_MARKER)) {
                string.append('h');
            }
        }

        if (minutes != 0 || string.length() != 0 || BinaryLogic.containsAll(flags, TimeFormatter.CLOCK)) {
            if (string.length() != 0) {
                string.append(':');
            }
            string.append(StringFormatter.fillStart(minutes + "", 2, "0"));
            if (BinaryLogic.containsNone(flags, TimeFormatter.HIDE_MARKER)) {
                string.append('m');
            }
        }
        if (BinaryLogic.containsNone(flags, TimeFormatter.HIDE_SECONDS)) {

            if (string.length() != 0) {
                string.append(':');
            }
            string.append(StringFormatter.fillStart(seconds + "", 2, "0"));
            if (BinaryLogic.containsNone(flags, TimeFormatter.HIDE_MARKER)) {
                string.append('s');
            }

        }
        return string.toString();
    }

    /**
     * formats (\\d+)\\w?:(\\d+) to ms
     * 
     * @param text
     * @return
     */
    public static long formatStringToMilliseconds(final String text) {
        final String[] found = new Regex(text, "(\\d+)\\w?:(\\d+)").getRow(0);
        if (found == null) { return 0; }
        int hours = Integer.parseInt(found[0]);
        int minutes = Integer.parseInt(found[1]);
        if (hours >= 24) {
            hours = 24;
            minutes = 0;
        }
        if (minutes >= 60) {
            hours += 1;
            minutes = 0;
        }
        return hours * 60 * 60 * 1000 + minutes * 60 * 1000;
    }

    public static long getMilliSeconds(final String wait) {
        String[][] matches = new Regex(wait, "([\\d]+) ?[\\.|\\,|\\:] ?([\\d]+)").getMatches();
        if (matches == null || matches.length == 0) {
            matches = new Regex(wait, Pattern.compile("([\\d]+)")).getMatches();
        }

        if (matches == null || matches.length == 0) { return -1; }

        double res = 0;
        if (matches[0].length == 1) {
            res = Double.parseDouble(matches[0][0]);
        }
        if (matches[0].length == 2) {
            res = Double.parseDouble(matches[0][0] + "." + matches[0][1]);
        }

        if (org.appwork.utils.Regex.matches(wait, Pattern.compile("(h|st)", Pattern.CASE_INSENSITIVE))) {
            res *= 60 * 60 * 1000l;
        } else if (org.appwork.utils.Regex.matches(wait, Pattern.compile("(m)", Pattern.CASE_INSENSITIVE))) {
            res *= 60 * 1000l;
        } else {
            res *= 1000l;
        }
        return Math.round(res);
    }

    public static long getMilliSeconds(final String dateString, final String timeformat, final Locale l) {
        if (dateString != null) {
            final SimpleDateFormat dateFormat = l != null ? new SimpleDateFormat(timeformat, l) : new SimpleDateFormat(timeformat);
            try {
                return dateFormat.parse(dateString).getTime();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static Date parseDateString(final String date) {
        if (date == null) { return null; }
        Date expireDate = null;
        for (final String format : TimeFormatter.dateformats) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.UK);
                sdf.setLenient(false);
                expireDate = sdf.parse(date);
                break;
            } catch (final Exception e2) {
            }
        }
        if (expireDate == null) { return null; }
        return expireDate;
    }
}
