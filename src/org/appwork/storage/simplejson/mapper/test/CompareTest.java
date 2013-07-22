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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.ParserException;
import org.appwork.storage.simplejson.mapper.JSonMapper;
import org.appwork.storage.simplejson.mapper.MapperException;

/**
 * @author thomas
 * 
 */
public class CompareTest {
    /**
     * @return
     */
    private static Object create() {
        TestClass ret = null;
        ret = TestClass.createObject();
        // ret = new TestClass();
        ret.getList().add(1);
        return ret;
    }

    public static void main(final String[] args) throws MapperException, ParserException {

        final JSonMapper mapper = new JSonMapper();
        final Object obj = CompareTest.create();
        final JSonNode json = mapper.create(obj);
        final String jsonString = json.toString();

        final TestClass re = (TestClass) mapper.jsonToObject(json, TestClass.class);
        // prints true if mapperloop succeeded

        System.out.println("SUCCESS: " + JSonStorage.serializeToJson(obj).equals(JSonStorage.serializeToJson(re)));
        System.out.println("SUCCESS: " + EqualsBuilder.reflectionEquals(obj, re));
    }

}
