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
    public static ArrayList<String> findUrls(String source) {
        /* TODO: better parsing */
        /* remove tags!! */
        final ArrayList<String> ret = new ArrayList<String>();
        try {

            for (String link : new Regex(source, "((https?|ftp):((//)|(\\\\\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)(\n|\r|$|<|\")").getColumn(0)) {
                try {
                    if (link != null) link = link.trim();
                    new URL(link);
                    if (!ret.contains(link)) ret.add(link);
                } catch (MalformedURLException e) {

                }
            }
        } catch (Exception e) {
            Log.exception(e);
        }
        return removeDuplicates(ret);
    }

    public static ArrayList<String> removeDuplicates(ArrayList<String> links) {
        ArrayList<String> tmplinks = new ArrayList<String>();
        if (links == null || links.size() == 0) return tmplinks;
        for (String link : links) {
            if (link.contains("...")) {
                String check = link.substring(0, link.indexOf("..."));
                String found = link;
                for (String link2 : links) {
                    if (link2.startsWith(check) && !link2.contains("...")) {
                        found = link2;
                        break;
                    }
                }
                if (!tmplinks.contains(found)) tmplinks.add(found);
            } else {
                tmplinks.add(link);
            }
        }
        return tmplinks;
    }

}
