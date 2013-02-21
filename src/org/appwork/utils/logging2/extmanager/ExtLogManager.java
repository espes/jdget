package org.appwork.utils.logging2.extmanager;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ExtLogManager extends LogManager {
    public static String[] WHITELIST     = new String[] { "org.fourthline" };
    public static String[] BLACKLIST     = new String[] { "org.fourthline", "org.fourthline.cling.registry.Registry", "org.fourthline.cling.model.message.header", "org.fourthline.cling.model.message.UpnpHeaders", "org.fourthline.cling.transport" };

    private LoggerFactory  LoggerFactory = null;

    public LoggerFactory getLoggerFactory() {
        return LoggerFactory;
    }

    public void setLoggerFactory(final LoggerFactory LoggerFactory) {
        this.LoggerFactory = LoggerFactory;
    }

    @Override
    public synchronized Logger getLogger(final String name) {

        if (LoggerFactory != null) {
            for (final String b : BLACKLIST) {
                if (name.startsWith(b)) {
                    System.out.println("Ignored (BL): " + name);
                    return super.getLogger(name);
                }
            }

            for (final String w : WHITELIST) {
                if (name.startsWith(w)) {
                    System.out.println("Redirect Logger (WL): " + name);
                    return LoggerFactory.getLogger(name);

                }
            }

        }
        System.out.println("Ignored: " + name);
        return super.getLogger(name);
    }

  

}
