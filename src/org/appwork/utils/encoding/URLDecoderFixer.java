/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.encoding
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.encoding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public class URLDecoderFixer extends URLDecoder {
    public static String decode(final String s, final String enc) throws UnsupportedEncodingException {

        boolean needToChange = false;
        final int numChars = s.length();
        final StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;

        if (enc.length() == 0) { throw new UnsupportedEncodingException("URLDecoderFixer: empty string enc parameter"); }
        boolean exceptionFixed = false;
        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
            case '+':
                sb.append(' ');
                i++;
                needToChange = true;
                break;
            case '%':
                /*
                 * Starting with this instance of %, process all consecutive
                 * substrings of the form %xy. Each substring %xy will yield a
                 * byte. Convert all consecutive bytes obtained this way to
                 * whatever character(s) they represent in the provided
                 * encoding.
                 */
                final int iBackup = i;
                try {
                    try {

                        // (numChars-i)/3 is an upper bound for the number
                        // of remaining bytes
                        if (bytes == null) {
                            bytes = new byte[(numChars - i) / 3];
                        }
                        int pos = 0;

                        while (i + 2 < numChars && c == '%') {
                            final int v = Integer.parseInt(s.substring(i + 1, i + 3), 16);
                            if (v < 0) { throw new IllegalArgumentException("URLDecoderFixer: Illegal hex characters in escape (%) pattern - negative value"); }
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < numChars) {
                                c = s.charAt(i);
                            }
                        }

                        // A trailing, incomplete byte encoding such as
                        // "%x" will cause an exception to be thrown

                        if (i < numChars && c == '%') { throw new IllegalArgumentException("URLDecoderFixer: Incomplete trailing escape (%) pattern"); }

                        sb.append(new String(bytes, 0, pos, enc));
                    } catch (final NumberFormatException e) {
                        throw new IllegalArgumentException("URLDecoderFixer: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                } catch (final IllegalArgumentException e) {
                    exceptionFixed = true;
                    i = iBackup;
                    sb.append(c);
                    i++;
                }
                needToChange = true;
                break;
            default:
                sb.append(c);
                i++;
                break;
            }
        }
        if (exceptionFixed) {
            Log.exception(new Exception("URLDecoderFixer: had to fix " + s));
        }
        return needToChange ? sb.toString() : s;
    }
}
