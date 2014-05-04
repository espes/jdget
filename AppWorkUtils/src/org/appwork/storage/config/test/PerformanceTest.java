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

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.logging.Log;

/**
 * @author thomas
 * 
 */
public class PerformanceTest {
    public static void main(final String[] args) throws InterruptedException {
        PerformanceObserver po = null;
        // po = new PerformanceObserver();

        try {
            final MyInterface jc = JsonConfig.create(MyInterface.class);

            long rounds = 1000000;
            run(rounds, true);
            po = new PerformanceObserver();
            run(rounds, false);
            po.print();
            System.out.println("TEST SUCCESSFULL");
        } catch (final RuntimeException e) {
            // seems like the interface is malformed
            Log.exception(e);

            System.out.println("TEST FAILED");
        }

        // System.out.println("ConfigTime: "+MyInterface.SH.getNanoTime()+" "+JsonConfig.create(MyInterface.class).getStorageHandler().getNanoTime());

        // System.exit(1);

    }

    /**
     * @param rounds
     * @param print
     */
    private static void run(long rounds, boolean print) {
        long t;

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            JsonConfig.create(MyInterface.class).getInt();
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns READ JSonConfig Mapping+Proxy");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.SH.getKeyHandler("Int").getValue();
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns READ by String key");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.CFG.getInt();
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns READ JSonConfig bypass - Static Instance");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.INT.getValue();
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns READ Proxy Bypass - static Keyhandler");
        TestObject o = new TestObject();
        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            o.getA();
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns READ Direct");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            JsonConfig.create(MyInterface.class).setInt(5);
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns WRITE JSonConfig Mapping+Proxy");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.SH.getKeyHandler("Int").setValue(5);
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns WRITE by String key");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.CFG.setInt(5);
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns WRITE JSonConfig bypass - Static Instance");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            MyInterface.INT.setValue(5);
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns WRITE Proxy Bypass - static Keyhandler");

        t = System.nanoTime();
        for (long x = 0; x < rounds; x++) {
            o.setA(5);
        }
        if (print) System.out.println((System.nanoTime() - t) / rounds + "\t ns WRITE Direct");

    }
}
