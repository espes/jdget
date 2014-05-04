/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.parser.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.parser.test;

import org.appwork.utils.parser.HTMLParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author thomas
 * 
 */
public class HTMLParserTest {
    public static class TestEntry {

        private String[] finds = null;
        private final String string;
        private final int urlsCount;

        /**
         * @param i
         * @param string
         */
        public TestEntry(final int i, final String string) {
            this(i, string, (String) null);
        }

        /**
         * @param i
         * @param string2
         * @param b
         */
        public TestEntry(final int i, final String string, final String... finds) {
            this.urlsCount = i;
            this.string = string;
            this.finds = finds;
        }

        public TestEntry(final String string, final String... finds) {
            this(finds.length, string, finds);
        }

        /**
         * @return the finds
         */
        public String[] getFinds() {
            return this.finds;
        }

        /**
         * @return the string
         */
        public String getString() {
            return this.string;
        }

        /**
         * @return the urlsCount
         */
        public int getUrlsCount() {
            return this.urlsCount;
        }

    }

    @Test
    public void test() {
        final TestEntry[] testStrings = new TestEntry[] {
        /*
         * ends with space
         */
        new TestEntry("http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip ", "http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip"),
        /*
         * Starts with space
         */
        new TestEntry(" http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip ", "http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip"),
        /*
         * starts end ends with space
         */
        new TestEntry(" http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip ", "http://www.rapidshare.com/files/410828702/jetty-distribution-8.0.0.M0.zip"),
        /*
         * Space included
         */
        new TestEntry("http://www.rapidshare.com/files/410828702/jetty-dis tribution-8.0.0.M0.zip", "http://www.rapidshare.com/files/410828702/jetty-dis"),
        /*
         * 
         */
        new TestEntry("http://www.rapidshare.com/files/410828702/jetty-dis%20tribution-8.0.0.M0.zip", "http://www.rapidshare.com/files/410828702/jetty-dis%20tribution-8.0.0.M0.zip"),
        /*
         * 
         */
        new TestEntry("http://www.google.comkkkk&url1=www://yahoo.com:1182/s/Homelll", "http://www.google.comkkkk&url1=www://yahoo.com:1182/s/Homelll"),
        /*
         * multiple and ftp
         */
        new TestEntry(3, "http://www.google.de http://google.de ftp://bla www.google.de google.de"),
        /*
         * auth
         */
        new TestEntry("http://user@www.google.de http://user:pass@google.de ", "http://user@www.google.de", "http://user:pass@google.de"),

        /*
         * port
         */
        new TestEntry("http://www.google.de:999/dds.html", "http://www.google.de:999/dds.html"),

        /*
         * ankor and parameters
         */
        new TestEntry("http://www.google.de:999/dds.html#a \r\nhttp://www.google.de:999/dds.html?abc=3&jd=6#b", "http://www.google.de:999/dds.html#a", "http://www.google.de:999/dds.html?abc=3&jd=6#b"),

        };

        for (final TestEntry e : testStrings) {
            final java.util.List<String> found = HTMLParser.findUrls(e.getString());
            Assert.assertTrue(found.size() == e.getUrlsCount());
            if (e.getFinds() != null) {
                for (int i = 0; i < e.getFinds().length; i++) {
                    final boolean equals = found.get(i).equalsIgnoreCase(e.getFinds()[i]);
                    Assert.assertTrue(equals);
                }
            }
        }

    }
}
