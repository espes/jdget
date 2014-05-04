package org.appwork.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Lists {

    /**
     * returns a list which has only unique values This is "order-safe"
     * 
     * @param <T>
     * @param history
     * @return
     */
    public static <T> ArrayList<T> unique(final Collection<T> list) {
        final HashSet<T> helper = new HashSet<T>();
        final ArrayList<T> ret = new ArrayList<T>(list.size());
        for (final T e : list) {
            if (helper.add(e)) {
                ret.add(e);
            }
        }

        return ret;
    }

}
