
import jd.controlling.AccountController;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorCrawler;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcollector.LinkOrigin;
import jd.controlling.linkcollector.LinkOriginDetails;
import jd.controlling.linkcrawler.CrawledLink;
import jd.plugins.DownloadLink;

// import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.extraction.ArchiveController;
import org.jdownloader.plugins.controller.crawler.CrawlerPluginController;
import org.jdownloader.plugins.controller.host.HostPluginController;

import org.jdownloader.settings.GeneralSettings;

public class JDGet implements LinkCollectorListener {
    public static void main(String args[]) {
        JDGet jdget = new JDGet();
        jdget.go();
    }

    public JDGet() {
        System.out.println("Init Host Plugins");
        HostPluginController.getInstance().invalidateCache();
        CrawlerPluginController.invalidateCache();

        HostPluginController.getInstance().ensureLoaded();

        /* load links */
        System.out.println("Init DownloadLinks");
        DownloadController.getInstance();//.initDownloadLinks();
        System.out.println("Init Linkgrabber");
        LinkCollector.getInstance();//.initLinkCollector();

        // ExtensionController.getInstance().invalidateCache();
        // ExtensionController.getInstance().init();

        AccountController.getInstance();
        ArchiveController.getInstance();

        /* start downloadwatchdog */
        System.out.println("Init DownloadWatchdog");
        DownloadWatchDog.getInstance();

        LinkCollector.getInstance().getEventsender().addListener(this, true);
    }

    public void go() {

        // DownloadWatchDog.getInstance().startDownloads();

         try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        addLink("http://cnn.com");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}
    }

    public void addLink(String s) {
        LinkCollector.getInstance().addCrawlerJob(new LinkCollectingJob(new LinkOriginDetails(LinkOrigin.START_PARAMETER, null), s));
    }


    public void onLinkCollectorAbort(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorAbort");
    }
    public void onLinkCollectorFilteredLinksAvailable(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorFilteredLinksAvailable");
    }
    public void onLinkCollectorFilteredLinksEmpty(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorFilteredLinksEmpty");
    }
    public void onLinkCollectorDataRefresh(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorDataRefresh");
    }
    public void onLinkCollectorStructureRefresh(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorStructureRefresh");
    }
    public void onLinkCollectorContentRemoved(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorContentRemoved");
    }
    public void onLinkCollectorContentAdded(LinkCollectorEvent event) {
        System.out.println("onLinkCollectorContentAdded");
    }
    public void onLinkCollectorLinkAdded(LinkCollectorEvent event, CrawledLink parameter) {
        System.out.println("onLinkCollectorLinkAdded");
    }
    public void onLinkCollectorDupeAdded(LinkCollectorEvent event, CrawledLink parameter) {
        System.out.println("onLinkCollectorDupeAdded");
    }
    public void onLinkCrawlerAdded(LinkCollectorCrawler parameter) {
        System.out.println("onLinkCrawlerAdded");
    }
    public void onLinkCrawlerStarted(LinkCollectorCrawler parameter) {
        System.out.println("onLinkCrawlerStarted");
    }
    public void onLinkCrawlerStopped(LinkCollectorCrawler parameter) {
        System.out.println("onLinkCrawlerStopped");
    }
}