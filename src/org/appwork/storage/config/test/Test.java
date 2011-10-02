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
import org.appwork.storage.config.ConfigEventListener;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.KeyHandler;
import org.appwork.storage.config.StorageHandler;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) throws InterruptedException {

        
        new PerformanceObserver().start();
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
//
//            jc.getStorageHandler().getEventSender().addListener(new ConfigEventListener() {
//
//                @Override
//                public void onConfigValidatorError(final Class<? extends ConfigInterface> config, final Throwable validateException, final KeyHandler methodHandler) {
//                    // TODO Auto-generated method stub
//
//                }
//
//                @Override
//                public void onConfigValueModified(final Class<? extends ConfigInterface> config, final String key, final Object newValue) {
//                    System.out.println("New value: " + key + "=\r\n" + JSonStorage.toString(newValue));
//                }
//            });
            
            
           
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

            System.out.println(JSonStorage.toString(jc.getIntArray()));
            System.out.println(JSonStorage.toString(jc.getObject()));
            System.out.println(JSonStorage.toString(jc.getGenericList()));

            System.out.println(JSonStorage.toString(jc.getStringArray()));
            
            /*
             * 4. get values by key
             * 
             * 
             * 
             */
            StorageHandler<?> storageHandler = MyInterface.CFG.getStorageHandler();
         System.out.println(storageHandler.getValue("Float"));  
            System.out.println(MyInterface.CFG.getInt());
            //Set Statics in the interface to use compiletime checks
          Integer i = storageHandler.getValue( MyInterface.INT);
            System.out.println(i);
            MyInterface.CFG.setInt(100);
            long rounds=1000000;
            long t;
             t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
                JsonConfig.create(MyInterface.class).getInt();
            }
            System.out.println("Complete access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               MyInterface.SH.getValue( "Int");
            }
            System.out.println("by (str) key access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
                MyInterface.CFG.getInt();
            }
            System.out.println("Static prxy access: \t"+(System.currentTimeMillis()-t));

            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               MyInterface.SH.getValue( MyInterface.INT);
            }
            System.out.println("Full st access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               o.getA();
            }
            System.out.println("direct cls access: \t"+(System.currentTimeMillis()-t));
            System.out.println("WRITE");
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
                JsonConfig.create(MyInterface.class).setInt(5);
            }
            System.out.println("Complete access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               MyInterface.SH.setValue( "Int",5);
            }
            System.out.println("by (str) key access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
                MyInterface.CFG.setInt(5);
            }
            System.out.println("Static prxy access: \t"+(System.currentTimeMillis()-t));

            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               MyInterface.SH.setValue( MyInterface.INT,5);
            }
            System.out.println("Full st access: \t"+(System.currentTimeMillis()-t));
            
            t = System.currentTimeMillis();
            for(long x=0;x<rounds;x++){
               o.setA(5);
            }
            System.out.println("direct cls access: \t"+(System.currentTimeMillis()-t));
            
            System.out.println("TEST SUCCESSFULL");
            
        } catch (final RuntimeException e) {
            // seems like the interface is malformed
            Log.exception(e);
            
            System.out.println("TEST FAILED");
        }
      
//        System.out.println("ConfigTime: "+MyInterface.SH.getNanoTime()+" "+JsonConfig.create(MyInterface.class).getStorageHandler().getNanoTime());
        
//System.exit(1);

    }
}
