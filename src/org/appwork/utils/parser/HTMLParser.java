/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.parser
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.appwork.utils.Regex;
import org.appwork.utils.logging.Log;

/**
 * @author coalado
 * 
 */
public class HTMLParser {
    public static ArrayList<URL> findUrls(String source) {
        final ArrayList<URL> ret = new ArrayList<URL>();
        try {
            for (String link : new Regex(source, "((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)").getColumn(0)) {
                try {
                    ret.add(new URL(link));
                } catch (MalformedURLException e) {

                }
            }
        } catch (Exception e) {
            Log.exception(e);
        }
        return ret;

    }

}
