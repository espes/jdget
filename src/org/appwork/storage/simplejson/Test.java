/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) {
        final String jsonString = "{\"app\" : \"JDownloader\", \"guiless\" : false, \"workingdirectory\" : \"\", \"restart\" : \"java -jar \\\"JDownloader.jar\\\"\", \"currentbranch\" : \"updater\", \"branch\" : null, \"list\" : [true, 1, {\"a\" : 3}, \"string\"], \"object\" : {\"debug\" : false, \"packagepollinterval\" : 5000, \"osfilter\" : true}, \"debug\" : false, \"packagepollinterval\" : 5000, \"osfilter2\" : true, \"osfilter\" : true}";

        final JSonNode json = JSonFactory.parse(jsonString);

        final String reString = json.toString();
        System.out.println(jsonString);
        System.out.println(reString);
        System.out.println(jsonString.length() - reString.length());
    }
}
