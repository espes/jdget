/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.jackson.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.jackson.test;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.jackson.JacksonMapper;

/**
 * @author thomas
 * 
 */
public class GenericTest {
    public static void main(final String[] args) {
        JSonStorage.setMapper(new JacksonMapper());
        final DataList ret = new DataList();
        ret.add(1);
        ret.add(2);

        final String str = JSonStorage.serializeToJson(ret);
        final DataList fl = JSonStorage.restoreFromString(str, new TypeRef<DataList>() {
        }, null);
        System.out.println(fl);
    }
}
