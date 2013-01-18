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
import org.appwork.storage.simplejson.JSonValue;
import org.appwork.storage.simplejson.ParserException;

/**
 * @author Thomas
 * 
 */
public class EscapeTest {
    public static void main(String[] args) {
        try {
            JSonValue value = new JSonValue("c:\\bla\\blabla");
            String toString = value.toString();
            JSonValue paresed;
            paresed = (JSonValue) new JSonFactory(toString).parse();
            System.out.println("Test OK: "+paresed.getValue().equals(value.getValue()));
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
