package jd.http;

import java.util.logging.Logger;

public class BrowserSettingsThread extends Thread implements BrowserSettings {

    private ProxySelectorInterface proxySelector;
    private boolean                debug;
    private boolean                verbose;
    protected Logger               logger;

    public BrowserSettingsThread() {

        copySettings();
    }

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
            @SuppressWarnings("unchecked")
            final BrowserSettings settings = (BrowserSettings) currentThread;
            proxySelector = settings.getProxySelector();
            debug = settings.isDebug();
            verbose = settings.isVerbose();
            logger = settings.getLogger();
        }
    }

    public ProxySelectorInterface getProxySelector() {
        return proxySelector;
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

    public void setProxySelector(final ProxySelectorInterface proxy) {
        this.proxySelector = proxy;
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
