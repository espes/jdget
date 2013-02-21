/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2.extmanager
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2.extmanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.appwork.utils.logging2.LogSourceProvider;

/**
 * @author Thomas
 * 
 */
public class LoggerFactory extends LogSourceProvider {
    private static final LoggerFactory INSTANCE = new LoggerFactory();
    static {

        try {
            // the logmanager should not be initialized here. so setting the
            // property should tell the logmanager to init a ExtLogManager
            // instance.
            System.setProperty("java.util.logging.manager", LoggerFactory.class.getName());

            ((ExtLogManager) LogManager.getLogManager()).setLoggerFactory(INSTANCE);
        } catch (final Throwable e) {
            e.printStackTrace();
            final LogManager lm = LogManager.getLogManager();
            System.err.println("Logmanager: " + lm);
            try {
                if (lm != null) {
                    // seems like the logmanager has already been set, and is
                    // not of type ExtLogManager. try to fix this here
                    // we experiences this bug once on a mac system. may be
                    // caused by mac jvm, or the mac install4j launcher

                    // 12.11:
                    // a winxp user had this problem with install4j (exe4j) as
                    // well.
                    // seems like 4xeej sets a logger before our main is
                    // reached.
                    final Field field = LogManager.class.getDeclaredField("manager");
                    field.setAccessible(true);
                    final ExtLogManager manager = new ExtLogManager();

                    field.set(null, manager);
                    final Field rootLogger = LogManager.class.getDeclaredField("rootLogger");
                    rootLogger.setAccessible(true);
                    final Logger rootLoggerInstance = (Logger) rootLogger.get(lm);
                    rootLogger.set(manager, rootLoggerInstance);
                    manager.addLogger(rootLoggerInstance);

                    // Adding the global Logger. Doing so in the Logger.<clinit>
                    // would deadlock with the LogManager.<clinit>.

                    final Method setLogManager = Logger.class.getDeclaredMethod("setLogManager", new Class[] { LogManager.class });
                    setLogManager.setAccessible(true);
                    setLogManager.invoke(Logger.global, manager);

                    final Enumeration<String> names = lm.getLoggerNames();
                    while (names.hasMoreElements()) {
                        manager.addLogger(lm.getLogger(names.nextElement()));

                    }
                }
            } catch (final Throwable e1) {
                e1.printStackTrace();
            }

        }
    }

    public static LoggerFactory I() {
        return INSTANCE;
    }

    private LoggerFactory() {
        super(System.currentTimeMillis());
    }

}
