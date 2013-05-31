package org.jdownloader.extensions.jdanywhere.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadControllerEvent;
import jd.controlling.downloadcontroller.DownloadControllerListener;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.event.DownloadWatchdogListener;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledLinkProperty;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.CrawledPackageProperty;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLinkProperty;
import jd.plugins.DownloadLinkProperty.Property;
import jd.plugins.FilePackage;
import jd.plugins.FilePackageProperty;
import jd.plugins.LinkStatus;

import org.appwork.controlling.StateEvent;
import org.appwork.controlling.StateEventListener;
import org.appwork.remoteapi.events.EventPublisher;
import org.appwork.remoteapi.events.EventsSender;
import org.appwork.remoteapi.events.SimpleEventObject;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.jdownloader.captcha.event.ChallengeResponseListener;
import org.jdownloader.captcha.v2.AbstractResponse;
import org.jdownloader.captcha.v2.ChallengeResponseController;
import org.jdownloader.captcha.v2.ChallengeSolver;
import org.jdownloader.captcha.v2.challenge.stringcaptcha.ImageCaptchaChallenge;
import org.jdownloader.captcha.v2.solverjob.SolverJob;
import org.jdownloader.extensions.jdanywhere.api.storable.CaptchaJob;
import org.jdownloader.settings.staticreferences.CFG_GENERAL;

public class JDAnywhereEventPublisher implements EventPublisher, DownloadWatchdogListener, DownloadControllerListener, StateEventListener, LinkCollectorListener, ChallengeResponseListener {

    private CopyOnWriteArraySet<EventsSender> eventSenders       = new CopyOnWriteArraySet<EventsSender>();
    private EventsAPI                         eventsApi          = new EventsAPI();
    HashMap<Long, String>                     linkStatusMessages = new HashMap<Long, String>();

    private enum EVENTID {
        SETTINGSCHANGED,
        LINKCHANGED,
        LINKSTATUSCHANGED,
        PACKAGEFINISHED,
        FILEPACKAGESTATUSCHANGED,
        DOWNLOADLINKADDED,
        DOWNLOADLINKREMOVED,
        DOWNLOADPACKAGEADDED,
        DOWNLOADPACKAGEREMOVED,
        CAPTCHA,
        RUNNINGSTATE,
        LINKCOLLECTORLINKADDED,
        LINKCOLLECTORLINKREMOVED,
        LINKCOLLECTORPACKAGEADDED,
        LINKCOLLECTORPACKAGEREMOVED,
        CRAWLEDLINKSTATUSCHANGED,
        CRAWLEDPACKAGESTATUSCHANGED
    }

    @Override
    public String[] getPublisherEventIDs() {
        return new String[] { EVENTID.SETTINGSCHANGED.name(), EVENTID.LINKCHANGED.name(), EVENTID.LINKSTATUSCHANGED.name(), EVENTID.FILEPACKAGESTATUSCHANGED.name(), EVENTID.PACKAGEFINISHED.name(), EVENTID.DOWNLOADLINKADDED.name(), EVENTID.DOWNLOADLINKREMOVED.name(), EVENTID.DOWNLOADPACKAGEADDED.name(), EVENTID.DOWNLOADPACKAGEREMOVED.name(), EVENTID.CAPTCHA.name(), EVENTID.RUNNINGSTATE.name(), EVENTID.LINKCOLLECTORLINKADDED.name(), EVENTID.LINKCOLLECTORLINKREMOVED.name(), EVENTID.LINKCOLLECTORPACKAGEADDED.name(), EVENTID.LINKCOLLECTORPACKAGEREMOVED.name(), EVENTID.CRAWLEDLINKSTATUSCHANGED.name(), EVENTID.CRAWLEDPACKAGESTATUSCHANGED.name() };
    }

    @Override
    public String getPublisherName() {
        return "jdanywhere";
    }

    GenericConfigEventListener<Integer> downloadSpeedLimitEventListener            = new GenericConfigEventListener<Integer>() {
                                                                                       public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "Limitspeed");
                                                                                           data.put("data", CFG_GENERAL.DOWNLOAD_SPEED_LIMIT.getValue());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                           // JDAnywhereController.getInstance().getEventsapi().publishEvent(new
                                                                                           // EventObject("SettingsChanged", data), null);
                                                                                       }

                                                                                       public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Boolean> downloadSpeedLimitEnabledEventListener     = new GenericConfigEventListener<Boolean>() {
                                                                                       public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                                                                                       }

                                                                                       public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "LimitspeedActivated");
                                                                                           data.put("data", CFG_GENERAL.DOWNLOAD_SPEED_LIMIT_ENABLED.isEnabled());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Integer> maxSimultaneDownloadsEventListenr          = new GenericConfigEventListener<Integer>() {
                                                                                       public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "MaxDL");
                                                                                           data.put("data", CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS.getValue());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }

                                                                                       public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Integer> maxChunksPerFileEventListener              = new GenericConfigEventListener<Integer>() {

                                                                                       public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "MaxConDL");
                                                                                           data.put("data", CFG_GENERAL.MAX_CHUNKS_PER_FILE.getValue());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }

                                                                                       public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Integer> maxSiumultaneDownloadsPerHostEventListener = new GenericConfigEventListener<Integer>() {

                                                                                       public void onConfigValueModified(KeyHandler<Integer> keyHandler, Integer newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "MaxConHost");
                                                                                           data.put("data", CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS_PER_HOST.getValue());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }

                                                                                       public void onConfigValidatorError(KeyHandler<Integer> keyHandler, Integer invalidValue, ValidationException validateException) {
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Boolean> maxDownloadsPerHostEnbledEventListener     = new GenericConfigEventListener<Boolean>() {

                                                                                       public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                                                                                       }

                                                                                       public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "MaxConHostActivated");
                                                                                           data.put("data", CFG_GENERAL.MAX_DOWNLOADS_PER_HOST_ENABLED.isEnabled());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Boolean> autioReconnectEnabledEventListener         = new GenericConfigEventListener<Boolean>() {

                                                                                       public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                                                                                       }

                                                                                       public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "Reconnect");
                                                                                           data.put("data", CFG_GENERAL.AUTO_RECONNECT_ENABLED.isEnabled());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }
                                                                                   };

    GenericConfigEventListener<Boolean> useAvailableAccountEventListener           = new GenericConfigEventListener<Boolean>() {

                                                                                       public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                                                                                       }

                                                                                       public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                                                                                           HashMap<String, Object> data = new HashMap<String, Object>();
                                                                                           data.put("message", "Premium");
                                                                                           data.put("data", CFG_GENERAL.USE_AVAILABLE_ACCOUNTS.isEnabled());
                                                                                           SimpleEventObject eventObject = new SimpleEventObject(JDAnywhereEventPublisher.this, EVENTID.SETTINGSCHANGED.name(), data);
                                                                                           for (EventsSender eventSender : eventSenders) {
                                                                                               eventSender.publishEvent(eventObject, null);
                                                                                           }
                                                                                       }
                                                                                   };

    private void publishEvent(EVENTID eventID, Object data) {
        SimpleEventObject eventObject = new SimpleEventObject(this, eventID.name(), data);
        for (EventsSender eventSender : eventSenders) {
            eventSender.publishEvent(eventObject, null);
        }
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void onDownloadControllerEvent(DownloadControllerEvent event) {
        switch (event.getType()) {
        case REFRESH_CONTENT:
        case REFRESH_STRUCTURE:
            if (event.getParameter() instanceof DownloadLink) {
                DownloadLink dl = (DownloadLink) event.getParameter();
                if (dl != null) {
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    Object param = event.getParameter(1);
                    if (param instanceof DownloadLinkProperty) {

                        if (((DownloadLinkProperty) param).getProperty() == Property.RESET) {
                            data.put("linkID", dl.getUniqueID().getID());
                            data.put("packageID", dl.getFilePackage().getUniqueID().toString());
                            data.put("action", "Reset");
                            publishEvent(EVENTID.LINKCHANGED, data);
                        } else {
                            data.put("linkID", dl.getUniqueID().getID());
                            data.put("packageID", dl.getFilePackage().getUniqueID().toString());
                            data.put("NewValue", ((DownloadLinkProperty) param).getValue());
                            switch (((DownloadLinkProperty) param).getProperty()) {
                            case NAME:
                                data.put("action", "NameChanged");
                                break;
                            case PRIORITY:
                                data.put("action", "PriorityChanged");
                                break;
                            case ENABLED:
                                data.put("action", "EnabledChanged");
                                break;
                            case AVAILABILITY:
                                data.put("action", "AvailabilityChanged");
                                break;
                            }
                            publishEvent(EVENTID.LINKSTATUSCHANGED, data);
                        }
                    } else {
                        LinkStatus linkStatus = dl.getLinkStatus();
                        if (linkStatus.getLatestStatus() == 2 && linkStatus.isPluginActive()) { // && linkStatus.getStatus() !=
                                                                                                // linkStatus.getLatestStatus()) {
                            data.put("linkID", dl.getUniqueID().getID());
                            data.put("packageID", dl.getFilePackage().getUniqueID().toString());
                            data.put("action", "Finished");
                            publishEvent(EVENTID.LINKCHANGED, data);
                            if (dl.getFilePackage().getFinishedDate() > 0) {
                                data = new HashMap<String, Object>();
                                data.put("packageID", dl.getFilePackage().getUniqueID().toString());
                                data.put("action", "PackageFinished");
                                publishEvent(EVENTID.PACKAGEFINISHED, data);
                            }
                        } else {
                            String lastMessage = linkStatusMessages.get(dl.getUniqueID().getID());
                            if (lastMessage == null) {
                                lastMessage = "";
                            }
                            String newMessage = linkStatus.getMessage(false);
                            if (newMessage == null) {
                                newMessage = "";
                            }
                            if (!lastMessage.equals(newMessage)) {
                                linkStatusMessages.remove(dl.getUniqueID().getID());
                                linkStatusMessages.put(dl.getUniqueID().getID(), newMessage);
                                data.put("action", "MessageChanged");
                                data.put("linkID", dl.getUniqueID().getID());

                                data.put("NewValue", newMessage);
                                publishEvent(EVENTID.LINKSTATUSCHANGED, data);
                            }
                        }
                    }
                }

            } else if (event.getParameter() instanceof FilePackage) {
                FilePackage dl = (FilePackage) event.getParameter();
                if (dl != null) {
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    Object param = event.getParameter(1);
                    if (param instanceof FilePackageProperty) {
                        data.put("packageID", dl.getUniqueID().toString());
                        data.put("NewValue", ((FilePackageProperty) param).getValue());
                        switch (((FilePackageProperty) param).getProperty()) {
                        case NAME:
                            data.put("action", "NameChanged");
                            break;
                        case FOLDER:
                            data.put("action", "FolderChanged");
                            break;
                        }
                        publishEvent(EVENTID.FILEPACKAGESTATUSCHANGED, data);
                    }
                }
            }
            break;
        case REMOVE_CONTENT:
            if (event.getParameters() != null) {
                for (Object link : (Object[]) event.getParameters()) {
                    if (link instanceof DownloadLink) downloadApiLinkRemoved((DownloadLink) link);
                    if (link instanceof FilePackage) downloadApiPackageRemoved((FilePackage) link);
                }
            }
            break;
        case ADD_CONTENT:
            if (event.getParameters() != null) {
                for (Object link : (Object[]) event.getParameters()) {
                    if (link instanceof DownloadLink) downloadApiLinkAdded((DownloadLink) link);
                    if (link instanceof FilePackage) downloadApiPackageAdded((FilePackage) link);
                }
            }
            break;
        }
    }

    private void downloadApiLinkAdded(DownloadLink link) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("linkID", link.getUniqueID().toString());
        data.put("packageID", link.getFilePackage().getUniqueID().toString());
        publishEvent(EVENTID.DOWNLOADLINKADDED, data);
    }

    private void downloadApiLinkRemoved(DownloadLink link) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("linkID", link.getUniqueID().toString());
        publishEvent(EVENTID.DOWNLOADLINKREMOVED, data);
    }

    private void downloadApiPackageAdded(FilePackage fpkg) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("packageID", fpkg.getUniqueID().toString());
        publishEvent(EVENTID.DOWNLOADPACKAGEADDED, data);
    }

    private void downloadApiPackageRemoved(FilePackage fpkg) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("packageID", fpkg.getUniqueID().toString());
        publishEvent(EVENTID.DOWNLOADPACKAGEREMOVED, data);
    }

    private void sendEvent(SolverJob<?> job, String type) {
        long captchCount = 0;
        for (SolverJob<?> entry : ChallengeResponseController.getInstance().listJobs()) {
            if (entry.isDone()) continue;
            if (entry.getChallenge() instanceof ImageCaptchaChallenge) {
                captchCount++;
            }
        }

        ImageCaptchaChallenge<?> challenge = (ImageCaptchaChallenge<?>) job.getChallenge();

        CaptchaJob apiJob = new CaptchaJob();
        if (challenge.getResultType().isAssignableFrom(String.class))
            apiJob.setType("Text");
        else
            apiJob.setType("Click");

        apiJob.setID(challenge.getId().getID());
        apiJob.setHoster(challenge.getPlugin().getHost());
        apiJob.setCaptchaCategory(challenge.getExplain());
        apiJob.setCount(captchCount);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("message", type);
        data.put("data", apiJob);
        publishEvent(EVENTID.CAPTCHA, data);

    }

    public void onStateChange(StateEvent event) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("message", "Running State Changed");
        data.put("data", event.getNewState().getLabel());
        publishEvent(EVENTID.RUNNINGSTATE, data);
    }

    public void onStateUpdate(StateEvent event) {
    }

    private void linkCollectorApiLinkAdded(CrawledLink link) {
        if (link.getParentNode().getUniqueID() != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("packageID", link.getParentNode().getUniqueID().toString());
            data.put("linkID", link.getUniqueID().toString());
            publishEvent(EVENTID.LINKCOLLECTORLINKADDED, data);
        }
    }

    private void linkCollectorApiLinkRemoved(List<CrawledLink> links) {
        List<String> linkIDs = new ArrayList<String>();
        for (CrawledLink link : links) {
            linkIDs.add(link.getDownloadLink().getUniqueID().toString());
        }

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("linkIDs", linkIDs);
        publishEvent(EVENTID.LINKCOLLECTORLINKREMOVED, data);
    }

    private void linkCollectorApiPackageAdded(CrawledPackage cpkg) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("packageID", cpkg.getUniqueID().toString());
        publishEvent(EVENTID.LINKCOLLECTORPACKAGEADDED, data);
    }

    private void linkCollectorApiPackageRemoved(CrawledPackage cpkg) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("packageID", cpkg.getUniqueID().toString());
        publishEvent(EVENTID.LINKCOLLECTORPACKAGEREMOVED, data);
    }

    @Override
    public void onLinkCollectorAbort(LinkCollectorEvent event) {
    }

    @Override
    public void onLinkCollectorFilteredLinksAvailable(LinkCollectorEvent event) {
    }

    @Override
    public void onLinkCollectorFilteredLinksEmpty(LinkCollectorEvent event) {
    }

    @Override
    public void onLinkCollectorDataRefresh(LinkCollectorEvent event) {
        if (event.getParameter() instanceof CrawledLink) {
            CrawledLink cl = (CrawledLink) event.getParameter();
            if (cl != null) {
                HashMap<String, Object> data = new HashMap<String, Object>();
                Object param = event.getParameter(1);
                if (param instanceof CrawledLinkProperty) {
                    data.put("linkID", cl.getUniqueID().getID());
                    data.put("packageID", cl.getParentNode().getUniqueID().toString());
                    data.put("NewValue", ((CrawledLinkProperty) param).getValue());
                    switch (((CrawledLinkProperty) param).getProperty()) {
                    case NAME:
                        data.put("action", "NameChanged");
                        break;
                    case PRIORITY:
                        data.put("action", "PriorityChanged");
                        break;
                    case ENABLED:
                        data.put("action", "EnabledChanged");
                        break;
                    case AVAILABILITY:
                        data.put("action", "AvailabilityChanged");
                        break;
                    }
                    publishEvent(EVENTID.CRAWLEDLINKSTATUSCHANGED, data);
                }
            }
        } else if (event.getParameter() instanceof CrawledPackage) {
            CrawledPackage cl = (CrawledPackage) event.getParameter();
            if (cl != null) {
                HashMap<String, Object> data = new HashMap<String, Object>();
                Object param = event.getParameter(1);
                if (param instanceof CrawledPackageProperty) {
                    data.put("packageID", cl.getUniqueID().toString());
                    data.put("NewValue", ((CrawledPackageProperty) param).getValue());
                    switch (((CrawledPackageProperty) param).getProperty()) {
                    case NAME:
                        data.put("action", "NameChanged");
                        break;
                    case FOLDER:
                        data.put("action", "FolderChanged");
                        break;
                    }
                    publishEvent(EVENTID.CRAWLEDPACKAGESTATUSCHANGED, data);
                }
            }
        }
    }

    @Override
    public void onLinkCollectorStructureRefresh(LinkCollectorEvent event) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLinkCollectorContentRemoved(LinkCollectorEvent event) {
        if (event.getParameters() != null) {
            for (Object object : event.getParameters()) {
                if (object instanceof List<?>) {
                    if (object != null && ((List<?>) object).get(0) instanceof CrawledLink) {
                        linkCollectorApiLinkRemoved((List<CrawledLink>) object);
                    }
                }
                if (object instanceof CrawledPackage) linkCollectorApiPackageRemoved((CrawledPackage) event.getParameter());
            }
        }
    }

    @Override
    public void onLinkCollectorContentAdded(LinkCollectorEvent event) {
        if (event.getParameters() != null) {
            for (Object object : event.getParameters()) {
                if (object instanceof CrawledLink) linkCollectorApiLinkAdded((CrawledLink) object);
                if (object instanceof CrawledPackage) linkCollectorApiPackageAdded((CrawledPackage) event.getParameter());
            }
        }
    }

    @Override
    public void onLinkCollectorContentModified(LinkCollectorEvent event) {
    }

    @Override
    public void onLinkCollectorLinkAdded(LinkCollectorEvent event, CrawledLink parameter) {
        linkCollectorApiLinkAdded((CrawledLink) parameter);
    }

    @Override
    public void onLinkCollectorDupeAdded(LinkCollectorEvent event, CrawledLink parameter) {
    }

    @Override
    public void onNewJobAnswer(SolverJob<?> job, AbstractResponse<?> response) {
    }

    @Override
    public void onJobDone(SolverJob<?> job) {
        sendEvent(job, "expired");
    }

    @Override
    public void onNewJob(SolverJob<?> job) {
        sendEvent(job, "new");
        eventsApi.sendNewCaptcha(job);
    }

    @Override
    public void onJobSolverEnd(ChallengeSolver<?> solver, SolverJob<?> job) {
    }

    @Override
    public void onJobSolverStart(ChallengeSolver<?> solver, SolverJob<?> job) {
    }

    @Override
    public void onLinkCollectorListLoaded() {
    }

    @Override
    public synchronized void register(EventsSender eventsAPI) {
        boolean wasEmpty = eventSenders.isEmpty();
        eventSenders.add(eventsAPI);
        if (wasEmpty && eventSenders.isEmpty() == false) {
            DownloadController.getInstance().addListener(this, true);
            ChallengeResponseController.getInstance().getEventSender().addListener(this);
            LinkCollector.getInstance().getEventsender().addListener(this, true);
            DownloadWatchDog.getInstance().getStateMachine().addListener(this);
            DownloadWatchDog.getInstance().getEventSender().addListener(this);
            CFG_GENERAL.DOWNLOAD_SPEED_LIMIT.getEventSender().addListener(downloadSpeedLimitEventListener, false);
            CFG_GENERAL.DOWNLOAD_SPEED_LIMIT_ENABLED.getEventSender().addListener(downloadSpeedLimitEnabledEventListener, false);
            CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS.getEventSender().addListener(maxSimultaneDownloadsEventListenr, false);
            CFG_GENERAL.MAX_CHUNKS_PER_FILE.getEventSender().addListener(maxChunksPerFileEventListener, false);
            CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS_PER_HOST.getEventSender().addListener(maxSiumultaneDownloadsPerHostEventListener, false);
            CFG_GENERAL.MAX_DOWNLOADS_PER_HOST_ENABLED.getEventSender().addListener(maxDownloadsPerHostEnbledEventListener, false);
            CFG_GENERAL.AUTO_RECONNECT_ENABLED.getEventSender().addListener(autioReconnectEnabledEventListener, false);
            CFG_GENERAL.USE_AVAILABLE_ACCOUNTS.getEventSender().addListener(useAvailableAccountEventListener, false);
        }
    }

    @Override
    public synchronized void unregister(EventsSender eventsAPI) {
        eventSenders.remove(eventsAPI);
        if (eventSenders.isEmpty()) {
            DownloadController.getInstance().removeListener(this);
            ChallengeResponseController.getInstance().getEventSender().removeListener(this);
            LinkCollector.getInstance().getEventsender().removeListener(this);
            DownloadWatchDog.getInstance().getStateMachine().removeListener(this);
            DownloadWatchDog.getInstance().getEventSender().removeListener(this);
            CFG_GENERAL.DOWNLOAD_SPEED_LIMIT.getEventSender().removeListener(downloadSpeedLimitEventListener);
            CFG_GENERAL.DOWNLOAD_SPEED_LIMIT_ENABLED.getEventSender().removeListener(downloadSpeedLimitEnabledEventListener);
            CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS.getEventSender().removeListener(maxSimultaneDownloadsEventListenr);
            CFG_GENERAL.MAX_CHUNKS_PER_FILE.getEventSender().removeListener(maxChunksPerFileEventListener);
            CFG_GENERAL.MAX_SIMULTANE_DOWNLOADS_PER_HOST.getEventSender().removeListener(maxSiumultaneDownloadsPerHostEventListener);
            CFG_GENERAL.MAX_DOWNLOADS_PER_HOST_ENABLED.getEventSender().removeListener(maxDownloadsPerHostEnbledEventListener);
            CFG_GENERAL.AUTO_RECONNECT_ENABLED.getEventSender().removeListener(autioReconnectEnabledEventListener);
            CFG_GENERAL.USE_AVAILABLE_ACCOUNTS.getEventSender().removeListener(useAvailableAccountEventListener);
        }
    }

    @Override
    public void onDownloadWatchdogDataUpdate() {
    }

    @Override
    public void onDownloadWatchdogStateIsIdle() {
    }

    @Override
    public void onDownloadWatchdogStateIsPause() {
    }

    @Override
    public void onDownloadWatchdogStateIsRunning() {
    }

    @Override
    public void onDownloadWatchdogStateIsStopped() {
    }

    @Override
    public void onDownloadWatchdogStateIsStopping() {
    }

}
