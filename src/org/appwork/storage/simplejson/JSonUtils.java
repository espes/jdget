/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson;

import java.util.Locale;

import org.appwork.utils.formatter.HexFormatter;

/**
 * @author thomas
 * 
 */
public class JSonUtils {

    public static String escape(final String s) {
        final StringBuilder sb = new StringBuilder();
        char ch;
        String ss;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            switch (ch) {
            case '"':
                sb.append("\\\"");
                continue;
            case '\\':
                sb.append("\\\\");
                continue;
            case '\b':
                sb.append("\\b");
                continue;
            case '\f':
                sb.append("\\f");
                continue;
            case '\n':
                sb.append("\\n");
                continue;
            case '\r':
                sb.append("\\r");
                continue;
            case '\t':
                sb.append("\\t");
                continue;

            }

            if (ch >= '\u0000' && ch <= '\u001F' || ch >= '\u007F' && ch <= '\u009F' || ch >= '\u2000' && ch <= '\u20FF') {
                ss = Integer.toHexString(ch);
                sb.append("\\u");
                for (int k = 0; k < 4 - ss.length(); k++) {
                    sb.append('0');
                }
                sb.append(ss.toUpperCase(Locale.ENGLISH));
                continue;
            }

            sb.append(ch);

        }
        return sb.toString();
    }

    public static void main(final String[] args) {
        final String str = "\\\r\t\b\n\f\"abc\u2011\u0026";
        for (int i = 0; i < str.length(); i++) {
            final String s = str.substring(i, i + 1);

            final String str2 = JSonUtils.escape(s);
            final String str3 = JSonUtils.unescape(str2);
            System.out.println(str3);
            System.err.println(s + " - " + HexFormatter.byteArrayToHex(s.getBytes()));
            System.out.println("OK: " + s + "|" + str3.equals(s));
        }

        final String s = str;

        final String str2 = JSonUtils.escape(s);
        final String str3 = JSonUtils.unescape(str2);
        System.out.println(HexFormatter.byteArrayToHex(str3.getBytes()));
        System.err.println(HexFormatter.byteArrayToHex(s.getBytes()));
        System.out.println("OK: |" + str3.equals(s));
        // System.err.println(str3);
    }

    /**
     * @param string
     * @return
     */
    public static String unescape(final String s) {
        char ch;
        final StringBuilder sb = new StringBuilder();
        final StringBuilder sb2 = new StringBuilder();
        int ii;
        int i;
        for (i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            switch (ch) {
            case '\\':
                ch = s.charAt(++i);
                switch (ch) {
                case '"':
                    sb.append('"');
                    continue;
                case '\\':
                    sb.append('\\');
                    continue;
                case 'r':
                    sb.append('\r');
                    continue;
                case 'n':
                    sb.append('\n');
                    continue;
                case 't':
                    sb.append('\t');
                    continue;
                case 'f':
                    sb.append('\f');
                    continue;
                case 'b':
                    sb.append('\b');
                    continue;

                case 'u':
                    sb2.delete(0, sb2.length());

                    i++;
                    ii = i + 4;
                    for (; i < ii; i++) {
                        ch = s.charAt(i);
                        if (sb2.length() > 0 || ch != '0') {
                            sb2.append(ch);
                        }
                    }
                    i--;
                    sb.append((char) Short.parseShort(sb2.toString(), 16));
                    continue;
                default:
                    sb.append(ch);
                    continue;
                }

            }
            sb.append(ch);
        }

        return sb.toString();
    }
}
