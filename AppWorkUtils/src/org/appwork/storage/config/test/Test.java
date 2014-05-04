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
import java.util.HashSet;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.events.ConfigEventListener;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) throws InterruptedException {

        // new PerformanceObserver().start();
        JsonConfig.create(MyInterface.class);
        /*
         * 1. Define an INterface with all required getters and setters. Use
         * Annotations to give defaultvalues or cryptinfos
         */
        /*
         * 2. Create your storage. The Factory will check your interface and
         * throw Exceptions if it is malformed. This helps to find Errors
         * immediatelly. The Check not only checks directly used Datatypes, but
         * runs through the whole TypeStructure of the interface.
         */

        try {
            final BadInterface jc = JsonConfig.create(BadInterface.class);
        } catch (final Throwable e) {

            Log.L.info("This exception is ok, because BadInterface is malformed due to various reasons");
            Log.exception(e);
        }
        try {
            final MyInterface jc = JsonConfig.create(MyInterface.class);

            HashSet<String> storedSet = jc.getSet();
            if (storedSet == null) {
                storedSet = new HashSet<String>();
            }
            storedSet.add(System.currentTimeMillis() + "");
            jc.setSet(storedSet);
            //
            jc._getStorageHandler().getEventSender().addListener(new ConfigEventListener() {

                @Override
                public void onConfigValidatorError(final KeyHandler<Object> keyHandler, final Object invalidValue, final ValidationException validateException) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onConfigValueModified(final KeyHandler<Object> keyHandler, final Object newValue) {
                    System.out.println("New value: " + keyHandler);

                }

            });
            MyInterface.INT.getEventSender().addListener(new GenericConfigEventListener<Integer>() {

                @Override
                public void onConfigValidatorError(final KeyHandler<Integer> keyHandler, final Integer invalidValue, final ValidationException validateException) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onConfigValueModified(final KeyHandler<Integer> keyHandler, final Integer newValue) {
                    // TODO Auto-generated method stub

                }

            });

            double[] ar = jc.getDoubleArray();
            jc.setDoubleArray(new double[] { 1.2, 3.4, 5.6 });
            System.out.println(JSonStorage.serializeToJson(jc.getObject()));
            ar = jc.getDoubleArray();
            /*
             * 3. Use getters and setters as if your storage would be a normal
             * instance.
             */
            final TestObject o = new TestObject();
            o.setA(36287);
            jc.setObject(o);
            jc.setIntArray(new int[] { 1, 2, 3, 4, 5 });

            final ArrayList<TestObject> list = new ArrayList<TestObject>();
            list.add(o);
            list.add(new TestObject());
            jc.setGenericList(list);
            jc.setObject(o);
            System.out.println(JSonStorage.serializeToJson(jc.getIntArray()));
            System.out.println(JSonStorage.serializeToJson(jc.getObject()));
            System.out.println(JSonStorage.serializeToJson(jc.getGenericList()));

            System.out.println(JSonStorage.serializeToJson(jc.getStringArray()));

            /*
             * 4. get values by key
             */

            final StorageHandler<?> storageHandler = MyInterface.CFG._getStorageHandler();
            System.out.println(storageHandler.getValue("Float"));
            System.out.println(MyInterface.CFG.getInt());
            // Set Statics in the interface to use compiletime checks
            /**
             * Validators
             */
            try {
                MyInterface.CFG.setInt(2000);
            } catch (final ValidationException e) {
                System.out.println("OK. 2000 is not valid for " + MyInterface.INT.getAnnotation(SpinnerValidator.class));
            }
            System.out.println("TEST SUCCESSFULL");
            /**
             * Defaults;
             * 
             * 
             * 
             */
            System.out.println(MyInterface.CFG.getDefault());
        } catch (final RuntimeException e) {
            // seems like the interface is malformed
            Log.exception(e);

            System.out.println("TEST FAILED");
        }

        // System.out.println("ConfigTime: "+MyInterface.SH.getNanoTime()+" "+JsonConfig.create(MyInterface.class).getStorageHandler().getNanoTime());

        System.exit(1);

    }
}
