package org.appwork.utils;

import java.util.Locale;

public class StringUtils {

    /**
     * Returns wether a String is null,empty, or contains whitespace only
     * 
     * @param ip
     * @return
     */
    public static boolean isEmpty(final String ip) {
        return ip == null || ip.trim().length() == 0;
    }

    /**
     * @param name
     * @param jdPkgRule
     * @return
     */
    public static boolean endsWithCaseInsensitive(final String name, final String jdPkgRule) {
        return name.toLowerCase(Locale.ENGLISH).endsWith(jdPkgRule.toLowerCase(Locale.ENGLISH));
    }

    /**
     * @param pass
     * @param pass2
     * @return
     */
    public static boolean equalsIgnoreCase(final String pass, final String pass2) {
        if (pass == pass2) {
            return true;
        }
        if (pass == null && pass2 != null) {
            return false;
        }
        return pass.equalsIgnoreCase(pass2);
    }

    /**
     * @param pass
     * @param pass2
     * @return
     */
    public static boolean equals(final String pass, final String pass2) {
        if (pass == pass2) {
            return true;
        }
        if (pass == null && pass2 != null) {
            return false;
        }
        return pass.equals(pass2);
    }

    /**
     * @param value
     * @return
     */
    public static boolean isNotEmpty(final String value) {
        return !isEmpty(value);
    }

    public static String fillPost(final String string, final String filler, final int minCount) {
        if (string.length() >= minCount) {
            return string;
        }

        final StringBuilder sb = new StringBuilder();

        sb.append(string);
        while (sb.length() < minCount) {
            sb.append(filler);
        }

        return sb.toString();
    }

    /**
     * @param sameSource
     * @param sourceUrl
     * @return
     */
    public static String getCommonalities(final String a, final String b) {
        if(a==null) {
            return b;
        }
        if(b==null) {
            return a;
        }
        int i=0;
        for ( i = 0; i < Math.min(a.length(), b.length()); i++) {
            if (a.charAt(i) != b.charAt(i)) {                
                return a.substring(0, i);
            }
        }
         return a.substring(0, i);
    }

}
