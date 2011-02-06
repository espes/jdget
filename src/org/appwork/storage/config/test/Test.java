/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import java.util.ArrayList;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.JsonConfig;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) throws ClassNotFoundException {

        final MyInterface jc = JsonConfig.create(MyInterface.class);
        final TestObject o = new TestObject();
        o.setA(36287);
        jc.setObject(o);
        jc.setIntArray(new int[] { 1, 2, 3, 4, 5 });

        final ArrayList<TestObject> list = new ArrayList<TestObject>();
        list.add(o);
        list.add(new TestObject());
        jc.setGenericList(list);

        System.out.println(JSonStorage.toString(jc.getIntArray()));
        System.out.println(JSonStorage.toString(jc.getObject()));
        System.out.println(JSonStorage.toString(jc.getGenericList()));
    }
}
