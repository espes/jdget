/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper.test;

import java.util.Date;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.simplejson.JSonFactory;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.mapper.JSonMapper;
import org.appwork.storage.simplejson.mapper.MapperException;
import org.appwork.storage.simplejson.mapper.TypeRef;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) throws MapperException {
        final Object obj = TestClass.createObject();
        final JSonMapper mapper = new JSonMapper();
        JSonNode json = mapper.create(obj);
        final String jsonString = json.toString();
        json = JSonFactory.parse(jsonString);

        final TestClass ss = mapper.jsonToObject(json, new TypeRef<TestClass>() {
        });

        System.out.println(mapper.create(ss));
    }

    /**
     * @throws MapperException
     * 
     */
    private static void testSerialize() throws MapperException {
        Object obj = TestClass.createObject();
        obj = new Date();
        final int iterations = 1000;
        final JSonMapper mapper = new JSonMapper();

        // mapper.create(obj);
        long t = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {

            final JSonNode node = mapper.create(obj);
            node.toString();
            // System.out.println(node);
        }
        final long self = System.currentTimeMillis() - t;
        System.out.println("Self: " + self + " ms");

        // JSonStorage.serializeToJson(obj);
        t = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            JSonStorage.serializeToJson(obj);
            // System.out.println();
        }
        final long jackson = System.currentTimeMillis() - t;
        System.out.println("Jackson: " + jackson + " ms");

    }
}
