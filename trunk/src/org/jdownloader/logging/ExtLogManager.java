package org.jdownloader.logging;

import java.util.logging.LogManager;
import java.util.logging.Logger;


public class ExtLogManager extends LogManager {
    private static String[] WHITELIST     = new String[] { "org.fourthline" };
    private static String[] BLACKLIST     = new String[] { "org.fourthline", "org.fourthline.cling.registry.Registry", "org.fourthline.cling.model.message.header", "org.fourthline.cling.model.message.UpnpHeaders", "org.fourthline.cling.transport" };

    private LogController   logController = null;

    public LogController getLogController() {
        return logController;
    }

    public void setLogController(LogController logController) {
        this.logController = logController;
    }

    @Override
    public synchronized Logger getLogger(String name) {

        if (logController != null) {
            for (String b : BLACKLIST) {
                if (name.startsWith(b)) {
                    System.out.println("Ignored (BL): " + name);
                    return super.getLogger(name);
                }
            }

            for (String w : WHITELIST) {
                if (name.startsWith(w)) {
                    System.out.println("Redirect Logger (WL): " + name);
                    return logController.getLogger(name);

                }
            }

        }
        System.out.println("Ignored: " + name);
        return super.getLogger(name);
    }

}
