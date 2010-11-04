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

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.Regex;

public class TimeFormatter {

    public static final int HIDE_SECONDS = 1 << 1;
    public static final int HIDE_MARKER  = 1 << 2;
    public static final int CLOCK        = 1 << 3;

    public static String formatMilliSeconds(long totalSeconds, int flags) {
        return formatSeconds(totalSeconds / 1000, flags);
    }

    public static String formatSeconds(long totalSeconds, int flags) {
        long days, hours, minutes, seconds;
        final StringBuilder string = new StringBuilder();

        days = totalSeconds / (24 * 60 * 60);
        totalSeconds -= days * 24 * 60 * 60;
        hours = totalSeconds / (60 * 60);
        totalSeconds -= hours * 60 * 60;
        minutes = totalSeconds / 60;
        seconds = totalSeconds - minutes * 60;

        if (!BinaryLogic.containsAll(flags, CLOCK)) {
            /*show days as extra field*/
            if (days != 0) {
                string.append(days);
                string.append('d');
            }
        } else {
            /*add days to hours field*/
            if (days != 0) {
                hours += days * 24;
            }
        }
        if (hours != 0 || string.length() != 0 || BinaryLogic.containsAll(flags, CLOCK)) {
            if (string.length() != 0) string.append(':');
            string.append(hours);
            if (BinaryLogic.containsNone(flags, HIDE_MARKER)) string.append('h');
        }

        if (minutes != 0 || string.length() != 0 || BinaryLogic.containsAll(flags, CLOCK)) {
            if (string.length() != 0) string.append(':');
            string.append(StringFormatter.fillStart(minutes + "", 2, "0"));
            if (BinaryLogic.containsNone(flags, HIDE_MARKER)) string.append('m');
        }
        if (BinaryLogic.containsNone(flags, HIDE_SECONDS)) {

            if (string.length() != 0) string.append(':');
            string.append(StringFormatter.fillStart(seconds + "", 2, "0"));
            if (BinaryLogic.containsNone(flags, HIDE_MARKER)) string.append('s');

        }
        return string.toString();
    }

    /**
     * formats (\\d+)\\w?:(\\d+) to ms
     * 
     * @param text
     * @return
     */
    public static long formatStringToMilliseconds(String text) {
        String[] found = new Regex(text, "(\\d+)\\w?:(\\d+)").getRow(0);
        if (found == null) return 0;
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
}
