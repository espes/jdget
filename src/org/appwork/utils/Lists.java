package org.appwork.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Lists {

    /**
     * returns a list which has only unique values
     * 
     * @param <T>
     * @param history
     * @return
     */
    public static <T> ArrayList<T> unique(final Collection<T> list) {
        final HashSet<T> helper = new HashSet<T>();
        helper.addAll(list);
        return new ArrayList<T>(helper);
    }

}
