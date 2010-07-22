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


/**
 * @author thomas
 * 
 */
public class ListFormatter {

    /**
     * @param string
     * @return
     */
    public static String toString(Iterable<?> list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(o);
        }
        return sb.toString();
    }

}
