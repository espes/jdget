/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.encoding
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.encoding;

import java.io.UnsupportedEncodingException;

/**
 * @author daniel
 * 
 */
public class URLEncode {

    private static final String RFC2396CHARS = "0123456789" + "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "-_.!~*'()";

    /* http://www.ietf.org/rfc/rfc2396.txt */
    public static String encodeRFC2396(final String input) throws UnsupportedEncodingException {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final char ch = input.charAt(i);
            if (ch == ' ') {
                sb.append("+");
            } else if (URLEncode.RFC2396CHARS.indexOf(ch) != -1) {
                sb.append(ch);
            } else {
                if (ch > 255) {
                    /* not allowed, replaced by + */
                    sb.append("+");
                } else {
                    /* hex formatted */
                    sb.append("%");
                    sb.append(Integer.toHexString(ch));

                }
            }
        }
        return sb.toString();
    }

}
