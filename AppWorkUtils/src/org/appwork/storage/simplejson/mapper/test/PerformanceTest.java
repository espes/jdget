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

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.jackson.JacksonMapper;
import org.appwork.storage.simplejson.JSonFactory;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.ParserException;
import org.appwork.storage.simplejson.mapper.JSonMapper;
import org.appwork.storage.simplejson.mapper.MapperException;

/**
 * @author thomas
 * 
 */
public class PerformanceTest {
    /**
     * @return
     */
    private static Object create() {
        TestClass ret = null;
        ret = TestClass.createObject();
        // ret = new TestClass();
        return ret;
    }

    public static void main(final String[] args) throws MapperException, ParserException {
        JSonStorage.setMapper(new JacksonMapper());
        for (int i = 1; i <= 100; i++) {
            PerformanceTest.testSerialize(i * 1);
            PerformanceTest.testDeserialize(i * 1);
        }

    }

    /**
     * @throws MapperException
     * @throws ParserException
     * 
     */
    private static void testDeserialize(final int iterations) throws MapperException, ParserException {
        final Object obj = PerformanceTest.create();

        final JSonMapper mapper = new JSonMapper();
        JSonNode json = mapper.create(obj);
        final String jsonString = json.toString();
        json = new JSonFactory(jsonString).parse();

        long t = System.currentTimeMillis();
        TestClass ss;
        for (int i = 0; i < iterations; i++) {
            json = new JSonFactory(jsonString).parse();
            ss = mapper.jsonToObject(json, new TypeRef<TestClass>() {
            });
            // System.out.println(node);
        }
        final long self = System.currentTimeMillis() - t;
        System.out.println("Des(awu)|" + iterations + ": " + self + "ms");

        // JSonStorage.serializeToJson(obj);
        t = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            ss = JSonStorage.restoreFromString(jsonString, new TypeRef<TestClass>() {
            }, null);
            // System.out.println();
        }
        final long jackson = System.currentTimeMillis() - t;
        System.out.println("Des(jackson)|" + iterations + ": " + jackson + "ms");
    }

    /**
     * @throws MapperException
     * 
     */
    private static void testSerialize(final int iterations) throws MapperException {
        final Object obj = PerformanceTest.create();

        final JSonMapper mapper = new JSonMapper();

        // mapper.create(obj);
        long t = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {

            final JSonNode node = mapper.create(obj);
            node.toString();
            // System.out.println(node);
        }
        final long self = System.currentTimeMillis() - t;
        System.out.println("Ser(awu)|" + iterations + ": " + self + "ms");

        // JSonStorage.serializeToJson(obj);
        t = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            JSonStorage.serializeToJson(obj);
            // System.out.println();
        }
        final long jackson = System.currentTimeMillis() - t;
        System.out.println("Ser(jackson)|" + iterations + ": " + jackson + "ms");
    }
}
