package org.appwork.utils.parser;

import java.util.HashMap;
import java.util.Map;

public class Mapimizer {

    /**
     * @param lines
     * @return
     */
    public static Map<String, String> keyValue(String sep, String... lines) {

        return keyValue(new HashMap<String, String>(), sep, lines);
    }

    public static Map<String, String> keyValue(String... lines) {

        return keyValue(new HashMap<String, String>(), "=", lines);
    }

    /**
     * @param sep
     * @param hashMap
     * @param lines
     * @return
     */
    private static Map<String, String> keyValue(Map<String, String> map, String sep, String... lines) {
        int index;
        for (String s : lines) {
            index = s.indexOf(sep);
            if (index < 0) throw new IllegalStateException(s + " does not contain " + sep);
            map.put(s.substring(0, index), s.substring(index + 1));
        }
        return map;
    }

}
