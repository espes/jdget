/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper.test;

import java.awt.Color;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.SimpleMapper;
import org.appwork.storage.TypeRef;
import org.appwork.storage.simplejson.mapper.ColorMapper;
import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class SpecialMappers {
    public static void main(final String[] args) {

        ((SimpleMapper) JSonStorage.getMapper()).getMapper().addMapper(Color.class, new ColorMapper());
        final String str = JSonStorage.serializeToJson(new SpecialObject(Application.getResource("list.txt"), SpecialMappers.class.getResource("SpecialMappers.class"), SpecialMappers.class));

        final SpecialObject obj = JSonStorage.restoreFromString(str, new TypeRef<SpecialObject>() {
        }, null);

        final String str2 = JSonStorage.serializeToJson(obj);
        if (str2.equals(str)) {
            System.out.println("OK");
            System.out.println(str);
            System.out.println(str2);
        } else {
            System.err.println("FAILED");
            System.err.println(str);
            System.err.println(str2);
        }
    }
}
