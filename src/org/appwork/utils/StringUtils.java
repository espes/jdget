package org.appwork.utils;

import java.util.Locale;

public class StringUtils {

    /**
     * Returns wether a String is null,empty, or contains whitespace only
     * @param ip
     * @return
     */
    public static boolean isEmpty(String ip) {     
        return ip==null||ip.trim().length()==0;
    }

    /**
     * @param name
     * @param jdPkgRule
     * @return
     */
    public static boolean endsWithCaseInsensitive(String name, String jdPkgRule) {
        return name.toLowerCase(Locale.ENGLISH).endsWith(jdPkgRule.toLowerCase(Locale.ENGLISH));
    }

}
