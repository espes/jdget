package jd.http;

import java.util.logging.Logger;

public class BrowserSettingsThread extends Thread implements BrowserSettings {

    private ProxySelectorInterface proxySelector;
    private boolean                debug;
    private boolean                verbose;
    protected Logger               logger;

    public BrowserSettingsThread() {

        this.copySettings();
    }

    public BrowserSettingsThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.copySettings();
    }

    public BrowserSettingsThread(ThreadGroup group, Runnable target) {
        super(group, target);
        this.copySettings();
    }

    public BrowserSettingsThread(ThreadGroup group, String name) {
        super(group, name);
        this.copySettings();
    }

    public BrowserSettingsThread(final Runnable r) {
        super(r);
        this.copySettings();
    }

    public BrowserSettingsThread(final Runnable r, final String name) {
        super(r, name);
        this.copySettings();
    }

    public BrowserSettingsThread(final String name) {
        super(name);
        this.copySettings();
    }

    private void copySettings() {
        final Thread currentThread = Thread.currentThread();
        /**
         * use BrowserSettings from current thread if available
         */
        if (currentThread != null && currentThread instanceof BrowserSettings) {
            @SuppressWarnings("unchecked")
            final BrowserSettings settings = (BrowserSettings) currentThread;
            this.proxySelector = settings.getProxySelector();
            this.debug = settings.isDebug();
            this.verbose = settings.isVerbose();
            this.logger = settings.getLogger();
        }
    }

    public ProxySelectorInterface getProxySelector() {
        return this.proxySelector;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void setProxySelector(final ProxySelectorInterface proxy) {
        this.proxySelector = proxy;
    }

    public void setDebug(final boolean b) {
        this.debug = b;
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public void setVerbose(final boolean b) {
        this.verbose = b;
    }

}
