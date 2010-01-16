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


public class TimeFormatter {

    protected static final int HIDE_SECONDS = 1;

    public static String formatSeconds(long totalSeconds, int flags) {
        long days, hours, minutes, seconds;
        final StringBuilder string = new StringBuilder();

        days = totalSeconds / (24 * 60 * 60);
        totalSeconds -= days * 24 * 60 * 60;
        hours = totalSeconds / (60 * 60);
        totalSeconds -= hours * 60 * 60;
        minutes = totalSeconds / 60;
        seconds = totalSeconds - minutes * 60;

        if (days != 0) string.append(days).append('d');
        if (hours != 0 || string.length() != 0) {
            if (string.length() != 0) string.append(':');
            string.append(hours).append('h');
        }
        if (minutes != 0 || string.length() != 0) {
            if (string.length() != 0) string.append(':');
            string.append(StringFormatter.fillStart(minutes + "", 2, "0")).append('m');
        }
        if ((flags & HIDE_SECONDS) == 0 || string.length() != 0) {
            if (string.length() != 0) string.append(':');
            string.append(StringFormatter.fillStart(seconds + "", 2, "0")).append('s');
        }
        return string.toString();
    }
}
