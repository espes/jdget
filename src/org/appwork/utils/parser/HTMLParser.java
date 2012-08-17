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
    public static java.util.List<String> findUrls(final String source) {
        /* TODO: better parsing */
        /* remove tags!! */

        final java.util.List<String> ret = new ArrayList<String>();
        try {

            for (String link : new Regex(source, "\\(?\\b(ftp://|https?://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]").getColumn(-1)) {
                try {
                    if (link != null) {
                        link = link.trim();
                    }
                    new URL(link);
                    if (!ret.contains(link)) {
                        ret.add(link);
                    }
                } catch (final MalformedURLException e) {

                }
            }
        } catch (final Exception e) {
            Log.exception(e);
        }
        return HTMLParser.removeDuplicates(ret);
    }

    public static java.util.List<String> removeDuplicates(final java.util.List<String> links) {
        final java.util.List<String> tmplinks = new ArrayList<String>();
        if ((links == null) || (links.size() == 0)) { return tmplinks; }
        for (final String link : links) {
            if (link.contains("...")) {
                final String check = link.substring(0, link.indexOf("..."));
                String found = link;
                for (final String link2 : links) {
                    if (link2.startsWith(check) && !link2.contains("...")) {
                        found = link2;
                        break;
                    }
                }
                if (!tmplinks.contains(found)) {
                    tmplinks.add(found);
                }
            } else {
                tmplinks.add(link);
            }
        }
        return tmplinks;
    }

}
