package org.jdownloader.gui.notify;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.downloadcontroller.event.DownloadWatchdogListener;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorCrawler;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.reconnect.Reconnecter;
import jd.controlling.reconnect.ReconnecterEvent;
import jd.controlling.reconnect.ReconnecterListener;
import jd.gui.swing.dialog.AbstractCaptchaDialog;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.components.toolbar.actions.UpdateAction;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.windowmanager.WindowManager;
import org.appwork.utils.swing.windowmanager.WindowManager.WindowExtendedState;
import org.jdownloader.captcha.event.ChallengeResponseListener;
import org.jdownloader.captcha.v2.AbstractResponse;
import org.jdownloader.captcha.v2.ChallengeResponseController;
import org.jdownloader.captcha.v2.ChallengeSolver;
import org.jdownloader.captcha.v2.solverjob.SolverJob;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.notify.gui.AbstractNotifyWindow;
import org.jdownloader.gui.notify.gui.Balloner;
import org.jdownloader.gui.notify.gui.BubbleNotifyConfig.Anchor;
import org.jdownloader.gui.notify.gui.BubbleNotifyConfigPanel;
import org.jdownloader.gui.notify.gui.CFG_BUBBLE;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.updatev2.InstallLog;
import org.jdownloader.updatev2.UpdateController;
import org.jdownloader.updatev2.UpdaterListener;

public class BubbleNotify implements UpdaterListener, ReconnecterListener, ChallengeResponseListener, DownloadWatchdogListener {
    private static final BubbleNotify INSTANCE = new BubbleNotify();

    /**
     * get the only existing instance of BubbleNotify. This is a singleton
     * 
     * @return
     */
    public static BubbleNotify getInstance() {
        return BubbleNotify.INSTANCE;
    }

    private Balloner ballooner;
    private boolean  updatesNotified;

    /**
     * Create a new instance of BubbleNotify. This is a singleton class. Access the only existing instance by using {@link #getInstance()}.
     */
    private BubbleNotify() {
        ballooner = new Balloner(null) {
            public JFrame getOwner() {
                if (JDGui.getInstance() == null) return null;
                return JDGui.getInstance().getMainFrame();
            }
        };
        ChallengeResponseController.getInstance().getEventSender().addListener(this);
        DownloadWatchDog.getInstance().getEventSender().addListener(this);
        GenericConfigEventListener<Object> update = new GenericConfigEventListener<Object>() {

            @Override
            public void onConfigValueModified(KeyHandler<Object> keyHandler, Object newValue) {
                ballooner.setScreenID(CFG_BUBBLE.CFG.getScreenID());

                if (CFG_BUBBLE.CFG.getAnimationStartPositionAnchor() == Anchor.SYSTEM_DEFAULT) {
                    switch (CrossSystem.getOS().getFamily()) {
                    case WINDOWS:
                        // bottom right position 10 pixel margin
                        ballooner.setStartPoint(new Point(-11, -1), Anchor.TOP_RIGHT);
                        break;
                    default:
                        // top right position 10 pixel margin
                        ballooner.setStartPoint(new Point(-11, 0), Anchor.BOTTOM_RIGHT);
                    }

                } else {
                    ballooner.setStartPoint(new Point(CFG_BUBBLE.CFG.getAnimationStartPositionX(), CFG_BUBBLE.CFG.getAnimationStartPositionY()), CFG_BUBBLE.CFG.getAnimationStartPositionAnchor());
                }

                if (CFG_BUBBLE.CFG.getAnimationEndPositionAnchor() == Anchor.SYSTEM_DEFAULT) {
                    switch (CrossSystem.getOS().getFamily()) {
                    case WINDOWS:
                        // move out to the right
                        ballooner.setEndPoint(new Point(-1, -11), Anchor.BOTTOM_LEFT);
                        break;
                    default:
                        // move out to the right
                        ballooner.setEndPoint(new Point(-1, 10), Anchor.TOP_LEFT);
                    }

                } else {
                    ballooner.setEndPoint(new Point(CFG_BUBBLE.CFG.getAnimationEndPositionX(), CFG_BUBBLE.CFG.getAnimationEndPositionY()), CFG_BUBBLE.CFG.getAnimationEndPositionAnchor());
                }

                if (CFG_BUBBLE.CFG.getFinalPositionAnchor() == Anchor.SYSTEM_DEFAULT) {
                    switch (CrossSystem.getOS().getFamily()) {
                    case WINDOWS:
                        ballooner.setAnchorPoint(new Point(-11, -11), Anchor.BOTTOM_RIGHT);
                        break;
                    default:
                        ballooner.setAnchorPoint(new Point(-11, 10), Anchor.TOP_RIGHT);
                    }

                } else {
                    ballooner.setAnchorPoint(new Point(CFG_BUBBLE.CFG.getFinalPositionX(), CFG_BUBBLE.CFG.getFinalPositionY()), CFG_BUBBLE.CFG.getFinalPositionAnchor());
                }

            }

            @Override
            public void onConfigValidatorError(KeyHandler<Object> keyHandler, Object invalidValue, ValidationException validateException) {
            }

        };

        CFG_BUBBLE.CFG._getStorageHandler().getEventSender().addListener(update);
        update.onConfigValueModified(null, null);
        initLinkCollectorListener();
        // if (ballooner != null) ballooner.add(new Notify(caption, text, NewTheme.I().getIcon("info", 32)));

        UpdateController.getInstance().getEventSender().addListener(this, true);

        Reconnecter.getInstance().getEventSender().addListener(this, true);

        synchronized (types) {

            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_newlinks3(), CFG_BUBBLE.BUBBLE_NOTIFY_ON_NEW_LINKGRABBER_LINKS_ENABLED));
            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_updates2(), CFG_BUBBLE.BUBBLE_NOTIFY_ON_UPDATE_AVAILABLE_ENABLED));
            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_reconnectstart3(), CFG_BUBBLE.BUBBLE_NOTIFY_ON_RECONNECT_START_ENABLED));

            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_captcha2(), CFG_BUBBLE.BUBBLE_NOTIFY_ON_CAPTCHA_IN_BACKGROUND_ENABLED));
            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_startstopdownloads2(), CFG_BUBBLE.BUBBLE_NOTIFY_START_STOP_DOWNLOADS_ENABLED));
            types.add(new BubbleType(_GUI._.plugins_optional_JDLightTray_ballon_startpausestop2(), CFG_BUBBLE.BUBBLE_NOTIFY_START_PAUSE_STOP_ENABLED));

        }
    }

    private void initLinkCollectorListener() {
        LinkCollector.getInstance().getEventsender().addListener(new LinkCollectorListener() {

            // @Override
            // public void onHighLight(CrawledLink parameter) {
            // if (!CFG_BUBBLE.BUBBLE_NOTIFY_ON_NEW_LINKGRABBER_LINKS_ENABLED.isEnabled()) return;
            // new EDTRunner() {
            //
            // @Override
            // protected void runInEDT() {
            // BasicNotify no = new BasicNotify(_GUI._.balloon_new_links(),
            // _GUI._.balloon_new_links_msg(LinkCollector.getInstance().getPackages().size(),
            // LinkCollector.getInstance().getChildrenCount()), NewTheme.I().getIcon(IconKey.ICON_LINKGRABBER, 32));
            // no.setActionListener(new ActionListener() {
            //
            // public void actionPerformed(ActionEvent e) {
            // JDGui.getInstance().requestPanel(JDGui.Panels.LINKGRABBER);
            // JDGui.getInstance().setFrameState(FrameState.TO_FRONT_FOCUSED);
            // }
            // });
            // show(no);
            // }
            // };
            // }
            //
            // @Override
            // public boolean isThisListenerEnabled() {
            // return CFG_BUBBLE.CFG.isBubbleNotifyOnNewLinkgrabberLinksEnabled();
            // }

            @Override
            public void onLinkCrawlerAdded(final LinkCollectorCrawler parameter) {
                if (!CFG_BUBBLE.BUBBLE_NOTIFY_ON_NEW_LINKGRABBER_LINKS_ENABLED.isEnabled()) return;

                // it is important to wait. we could miss events if we do not wait
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        LinkCrawlerBubble no = new LinkCrawlerBubble(parameter);
                        parameter.getEventSender().addListener(no, true);

                    }
                }.waitForEDT();

            }

            @Override
            public void onLinkCrawlerStarted(LinkCollectorCrawler parameter) {

            }

            @Override
            public void onLinkCrawlerStopped(LinkCollectorCrawler parameter) {
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
            }

            @Override
            public void onLinkCollectorStructureRefresh(LinkCollectorEvent event) {
            }

            @Override
            public void onLinkCollectorContentRemoved(LinkCollectorEvent event) {
            }

            @Override
            public void onLinkCollectorContentAdded(LinkCollectorEvent event) {
            }

            @Override
            public void onLinkCollectorLinkAdded(LinkCollectorEvent event, CrawledLink parameter) {
            }

            @Override
            public void onLinkCollectorDupeAdded(LinkCollectorEvent event, CrawledLink parameter) {
            }
        });

    }

    public void show(final AbstractNotifyWindow no) {
        if (JDGui.getInstance().isSilentModeActive() && !CFG_BUBBLE.BUBBLE_NOTIFY_ENABLED_DURING_SILENT_MODE.isEnabled()) return;

        new EDTRunner() {

            @Override
            protected void runInEDT() {

                switch (CFG_BUBBLE.CFG.getBubbleNotifyEnabledState()) {
                case JD_NOT_ACTIVE:
                    if (WindowManager.getInstance().hasFocus()) { return; }
                    break;

                case NEVER:
                    return;
                case TASKBAR:
                    if (WindowManager.getInstance().getExtendedState(JDGui.getInstance().getMainFrame()) != WindowExtendedState.ICONIFIED) { return; }
                    break;

                case TRAY:
                    if (!JDGui.getInstance().getMainFrame().isVisible()) break;
                    return;
                case TRAY_OR_TASKBAR:

                    if (WindowManager.getInstance().getExtendedState(JDGui.getInstance().getMainFrame()) == WindowExtendedState.ICONIFIED) {
                        break;
                    }
                    if (!JDGui.getInstance().getMainFrame().isVisible()) break;
                    return;
                }
                ballooner.add(no);
            }
        };

    }

    @Override
    public void onUpdatesAvailable(boolean selfupdate, InstallLog installlog) {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_ON_UPDATE_AVAILABLE_ENABLED.isEnabled()) return;
        if (UpdateController.getInstance().hasPendingUpdates() && !updatesNotified) {
            updatesNotified = true;
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    BasicNotify no = new BasicNotify(_GUI._.balloon_updates(), _GUI._.balloon_updates_msg(), NewTheme.I().getIcon("update", 32));
                    no.setActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            new UpdateAction().actionPerformed(e);
                        }
                    });
                    show(no);
                }
            };
        } else if (!UpdateController.getInstance().hasPendingUpdates()) {
            updatesNotified = false;
        }
    }

    @Override
    public void onBeforeReconnect(final ReconnecterEvent event) {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_ON_RECONNECT_START_ENABLED.isEnabled()) return;
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                show(new ReconnectBubble());
            }
        };
    }

    @Override
    public void onAfterReconnect(final ReconnecterEvent event) {

    }

    public void hide(final AbstractNotifyWindow notify) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ballooner.hide(notify);
            }
        };

    }

    @Override
    public void onNewJobAnswer(SolverJob<?> job, AbstractResponse<?> response) {
    }

    @Override
    public void onJobDone(SolverJob<?> job) {
    }

    @Override
    public void onNewJob(SolverJob<?> job) {
        switch (AbstractCaptchaDialog.getWindowState()) {
        case TO_BACK:
        case OS_DEFAULT:

            CaptchaNotify notify = new CaptchaNotify(job);
            show(notify);
        }
    }

    @Override
    public void onJobSolverEnd(ChallengeSolver<?> solver, SolverJob<?> job) {
    }

    @Override
    public void onJobSolverStart(ChallengeSolver<?> solver, SolverJob<?> job) {
    }

    public void relayout() {
        ballooner.relayout();
    }

    @Override
    public void onDownloadWatchdogDataUpdate() {
    }

    @Override
    public void onDownloadWatchdogStateIsIdle() {
    }

    @Override
    public void onDownloadWatchdogStateIsPause() {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_START_PAUSE_STOP_ENABLED.isEnabled()) {

        return; }
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                BasicNotify no = new BasicNotify(_GUI._.download_paused(), _GUI._.download_paused_msg(), NewTheme.I().getIcon(IconKey.ICON_MEDIA_PLAYBACK_PAUSE, 24));
                show(no);
            }
        };
    }

    @Override
    public void onDownloadWatchdogStateIsRunning() {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_START_PAUSE_STOP_ENABLED.isEnabled()) {

        return; }
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                BasicNotify no = new BasicNotify(_GUI._.download_start(), _GUI._.download_start_msg(), NewTheme.I().getIcon(IconKey.ICON_MEDIA_PLAYBACK_START, 24));
                show(no);
            }
        };
    }

    @Override
    public void onDownloadWatchdogStateIsStopped() {

        if (!CFG_BUBBLE.BUBBLE_NOTIFY_START_PAUSE_STOP_ENABLED.isEnabled()) {

        return; }
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                BasicNotify no = new BasicNotify(_GUI._.download_stopped(), _GUI._.download_stopped_msg(), NewTheme.I().getIcon(IconKey.ICON_MEDIA_PLAYBACK_STOP, 24));
                show(no);
            }
        };
    }

    @Override
    public void onDownloadWatchdogStateIsStopping() {

    }

    private class QueuedStart {
        public QueuedStart(SingleDownloadController downloadController) {
            controller = downloadController;
            this.time = System.currentTimeMillis();
        }

        private SingleDownloadController controller;
        private long                     time;
    }

    private LinkedList<QueuedStart>           queue   = new LinkedList<QueuedStart>();
    private Thread                            queueWorker;
    private HashSet<SingleDownloadController> started = new HashSet<SingleDownloadController>();

    @Override
    public void onDownloadControllerStart(final SingleDownloadController downloadController) {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_START_STOP_DOWNLOADS_ENABLED.isEnabled()) {
            started.clear();
            queue.clear();
            return;
        }
        synchronized (queue) {
            queue.add(new QueuedStart(downloadController));
            if (queueWorker == null) {
                queueWorker = new Thread("BubbleNotofyDelayerQUeue") {
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                return;
                            }
                            synchronized (queue) {

                                for (Iterator<QueuedStart> it = queue.iterator(); it.hasNext();) {
                                    final QueuedStart next = it.next();
                                    if (!next.controller.isActive()) {
                                        it.remove();
                                    } else if (System.currentTimeMillis() - next.time > CFG_BUBBLE.CFG.getDownloadStartEndNotifyDelay()) {
                                        it.remove();
                                        started.add(next.controller);
                                        new EDTRunner() {
                                            @Override
                                            protected void runInEDT() {

                                                DownloadStartedNotify no = new DownloadStartedNotify(next.controller);
                                                show(no);
                                            }
                                        };
                                    }
                                }
                                if (queue.size() == 0) {
                                    queueWorker = null;
                                    return;
                                }
                            }
                        }
                    }
                };
                queueWorker.start();
            }
        }

    }

    @Override
    public void onDownloadControllerStopped(final SingleDownloadController downloadController) {
        if (!CFG_BUBBLE.BUBBLE_NOTIFY_START_STOP_DOWNLOADS_ENABLED.isEnabled()) {
            started.clear();
            queue.clear();
            return;
        }
        synchronized (queue) {
            if (!started.contains(downloadController)) return;
            started.remove(downloadController);
        }
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                DownloadStoppedNotify no = new DownloadStoppedNotify(downloadController);
                show(no);
            }
        };
    }

    public void registerType(BubbleType type) {
        synchronized (types) {
            types.add(type);
        }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                if (configPanel != null) {
                    configPanel.updateTypes(getTypes());
                }
            }
        };
    }

    private List<BubbleType>        types = new ArrayList<BubbleType>();
    private BubbleNotifyConfigPanel configPanel;

    public List<BubbleType> getTypes() {
        synchronized (types) {
            return new ArrayList<BubbleType>(types);
        }
    }

    public BubbleNotifyConfigPanel getConfigPanel() {
        if (configPanel == null) {
            configPanel = new BubbleNotifyConfigPanel();
        }
        return configPanel;
    }
}
