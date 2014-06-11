package jd.controlling.linkcrawler;

import java.util.concurrent.atomic.AtomicInteger;

import jd.http.BrowserSettingsThread;
import jd.plugins.PluginForDecrypt;

import org.jdownloader.plugins.controller.PluginClassLoader;

public class LinkCrawlerThread extends BrowserSettingsThread {

    private static AtomicInteger linkCrawlerThread = new AtomicInteger(0);
    private LinkCrawler          crawler;
    private Object               owner             = null;

    public LinkCrawlerThread(Runnable r) {
        super(r);
        setName("LinkCrawler:" + linkCrawlerThread.incrementAndGet());
        setDaemon(true);
        setPriority(MIN_PRIORITY);
    }

    protected void setCurrentLinkCrawler(LinkCrawler crawler) {
        if (crawler != null) {
            PluginClassLoader.setThreadPluginClassLoaderChild(crawler.getPluginClassLoaderChild(), null);
        } else {
            PluginClassLoader.setThreadPluginClassLoaderChild(null, null);
        }
        this.crawler = crawler;
    }

    public LinkCrawler getCurrentLinkCrawler() {
        return crawler;
    }

    /**
     * @return the owner
     */
    public Object getCurrentOwner() {
        return owner;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setCurrentOwner(Object owner) {
        this.owner = owner;
    }

    public void setActivePlugin(PluginForDecrypt wplg) {
    }

}
