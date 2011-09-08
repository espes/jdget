package org.appwork.utils;

public class StringUtils {

    /**
     * Returns wether a String is null,empty, or contains whitespace only
     * @param ip
     * @return
     */
    public static boolean isEmpty(String ip) {     
        return ip==null||ip.trim().length()==0;
    }

}
