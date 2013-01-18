/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.test;

import org.appwork.storage.simplejson.JSonFactory;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.ParserException;

/**
 * @author Thomas
 * 
 */
public class ComaTest {
    public static void main(String[] args) {
        String str;
        try {

            JSonNode paresed = new JSonFactory(str = "{\"bla\":true,}").parse();
            System.out.println(paresed.toString());

            new ParserException(str + " is invalid and should throw ayn exception").printStackTrace();
        } catch (ParserException e) {
            try {
                e.printStackTrace();
                JSonNode paresed = new JSonFactory(str = "{}").parse();
                System.out.println(paresed.toString());
                paresed = new JSonFactory(str = "{  }").parse();
                System.out.println(paresed.toString());
                System.out.println("TEST OK");
            } catch (ParserException e1) {
                e1.printStackTrace();
            }
        }
    }
}
