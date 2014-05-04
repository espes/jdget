package jd.http;

import java.util.logging.Logger;

import org.appwork.utils.net.httpconnection.HTTPProxy;

public class BrowserSettingsThread extends Thread implements BrowserSettings {

    private HTTPProxy proxy;
    private boolean   debug;
    private boolean   verbose;
    protected Logger  logger;
   

    public BrowserSettingsThread(final Runnable r) {
        super(r);
        copySettings();
    }

    public BrowserSettingsThread(final Runnable r, final String name) {
        super(r, name);
        copySettings();
    }

    public BrowserSettingsThread(final String name) {
        super(name);
        copySettings();
    }

    private void copySettings() {
        final Thread currentThread = Thread.currentThread();
        /**
         * use BrowserSettings from current thread if available
         */
        if (currentThread != null && currentThread instanceof BrowserSettings) {
            final BrowserSettings settings = (BrowserSettings) currentThread;
            proxy = settings.getCurrentProxy();
            debug = settings.isDebug();
            verbose = settings.isVerbose();
            logger = settings.getLogger();
        }
    }

    public HTTPProxy getCurrentProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setCurrentProxy(final HTTPProxy proxy) {
        this.proxy = proxy;
    }

    public void setDebug(final boolean b) {
        debug = b;
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public void setVerbose(final boolean b) {
        verbose = b;
    }

}
