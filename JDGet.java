
import java.util.List;

import jd.controlling.AccountController;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.DownloadLinkCandidate;
import jd.controlling.downloadcontroller.DownloadLinkCandidateResult;
import jd.controlling.downloadcontroller.DownloadWatchDogProperty;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.downloadcontroller.event.DownloadWatchdogListener;
import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorCrawler;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcollector.LinkOrigin;
import jd.controlling.linkcollector.LinkOriginDetails;
import jd.controlling.linkcollector.event.LinkCollectorCrawlerListener;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLinkProperty;
import jd.plugins.FilePackage;
import jd.plugins.FilePackageProperty;
import jd.plugins.LinkStatusProperty;

// import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.extraction.ArchiveController;
import org.jdownloader.plugins.controller.crawler.CrawlerPluginController;
import org.jdownloader.plugins.controller.host.HostPluginController;

import org.jdownloader.controlling.download.DownloadControllerListener;


import org.jdownloader.settings.GeneralSettings;

public class JDGet implements LinkCollectorListener, LinkCollectorCrawlerListener, DownloadControllerListener, DownloadWatchdogListener {
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
        DownloadController.getInstance().addListener(this, true);
    }

    public void go() {

        DownloadWatchDog.getInstance().startDownloads();

         try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        addLink("http://tpg.com.au/test3.iso");
        System.out.println("addLink done");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}
    }

    public void addLink(String s) {
        LinkCrawler lc = LinkCollector.getInstance().addCrawlerJob(
            new LinkCollectingJob(new LinkOriginDetails(LinkOrigin.START_PARAMETER, null), s));
        lc.waitForCrawling();
        // LinkCollector.getInstance().getLinkChecker().waitForChecked();
    }



    // LinkCollectorListener
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
        parameter.getEventSender().addListener(this, true);
    }
    public void onLinkCrawlerStarted(LinkCollectorCrawler parameter) {
        System.out.println("onLinkCrawlerStarted");
    }
    public void onLinkCrawlerStopped(LinkCollectorCrawler parameter) {
        System.out.println("onLinkCrawlerStopped");
    }


    // LinkCollectorCrawlerListener
    public void onProcessingCrawlerPlugin(LinkCollectorCrawler caller, CrawledLink parameter) {
        System.out.println("onProcessingCrawlerPlugin");
    }
    public void onProcessingHosterPlugin(LinkCollectorCrawler caller, CrawledLink parameter) {
        System.out.println("onProcessingHosterPlugin");
    }
    public void onProcessingContainerPlugin(LinkCollectorCrawler caller, CrawledLink parameter) {
        System.out.println("onProcessingContainerPlugin");
    }


    // DownloadControllerListener
    public void onDownloadControllerAddedPackage(FilePackage pkg) {
        System.out.println("onDownloadControllerAddedPackage");
    }
    public void onDownloadControllerStructureRefresh(FilePackage pkg) {
        System.out.println("onDownloadControllerStructureRefresh1");
    }
    public void onDownloadControllerStructureRefresh() {
        System.out.println("onDownloadControllerStructureRefresh2");
    }
    public void onDownloadControllerStructureRefresh(AbstractNode node, Object param) {
        System.out.println("onDownloadControllerStructureRefresh3");
    }
    public void onDownloadControllerRemovedPackage(FilePackage pkg) {
        System.out.println("onDownloadControllerRemovedPackage");
    }
    public void onDownloadControllerRemovedLinklist(List<DownloadLink> list) {
        System.out.println("onDownloadControllerRemovedLinklist");
    }
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink, DownloadLinkProperty property) {
        System.out.println("onDownloadControllerUpdatedData1");
    }
    public void onDownloadControllerUpdatedData(FilePackage pkg, FilePackageProperty property) {
        System.out.println("onDownloadControllerUpdatedData2");
    }
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink, LinkStatusProperty property) {
        System.out.println("onDownloadControllerUpdatedData3");
    }
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink) {
        System.out.println("onDownloadControllerUpdatedData4");
    }
    public void onDownloadControllerUpdatedData(FilePackage pkg) {
        System.out.println("onDownloadControllerUpdatedData5");
    }

    // DownloadWatchdogListener
    public void onDownloadWatchdogDataUpdate(){
        System.out.println("onDownloadWatchdogDataUpdate");
    }
    public void onDownloadWatchdogStateIsIdle(){
        System.out.println("onDownloadWatchdogStateIsIdle");
    }
    public void onDownloadWatchdogStateIsPause(){
        System.out.println("onDownloadWatchdogStateIsPause");
    }
    public void onDownloadWatchdogStateIsRunning(){
        System.out.println("onDownloadWatchdogStateIsRunning");
    }
    public void onDownloadWatchdogStateIsStopped(){
        System.out.println("onDownloadWatchdogStateIsStopped");
    }
    public void onDownloadWatchdogStateIsStopping(){
        System.out.println("onDownloadWatchdogStateIsStopping");
    }
    public void onDownloadControllerStart(SingleDownloadController downloadController, DownloadLinkCandidate candidate){
        System.out.println("onDownloadControllerStart");
    }
    public void onDownloadControllerStopped(SingleDownloadController downloadController, DownloadLinkCandidate candidate, DownloadLinkCandidateResult result){
        System.out.println("onDownloadControllerStopped");
    }
    public void onDownloadWatchDogPropertyChange(DownloadWatchDogProperty propertyChange){
        System.out.println("onDownloadWatchDogPropertyChange");
    }
}