package org.jdownloader.gui.notify;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorHighlightListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.reconnect.Reconnecter;
import jd.controlling.reconnect.ReconnecterEvent;
import jd.controlling.reconnect.ReconnecterListener;
import jd.controlling.reconnect.ipcheck.IPController;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.components.toolbar.actions.UpdateAction;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.WindowManager.FrameState;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.notify.gui.AbstractNotifyWindow;
import org.jdownloader.gui.notify.gui.Balloner;
import org.jdownloader.gui.notify.gui.BubbleNotifyConfig.Anchor;
import org.jdownloader.gui.notify.gui.CFG_BUBBLE;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.updatev2.InstallLog;
import org.jdownloader.updatev2.UpdateController;
import org.jdownloader.updatev2.UpdaterListener;

public class BubbleNotify implements UpdaterListener, ReconnecterListener {
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
    }

    private void initLinkCollectorListener() {
        LinkCollector.getInstance().getEventsender().addListener(new LinkCollectorHighlightListener() {

            @Override
            public void onHighLight(CrawledLink parameter) {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        BasicNotify no = new BasicNotify(CFG_BUBBLE.BUBBLE_NOTIFY_ON_NEW_LINKGRABBER_LINKS_ENABLED, _GUI._.balloon_new_links(), _GUI._.balloon_new_links_msg(LinkCollector.getInstance().getPackages().size(), LinkCollector.getInstance().getChildrenCount()), NewTheme.I().getIcon(IconKey.ICON_LINKGRABBER, 32));
                        no.setActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                JDGui.getInstance().requestPanel(JDGui.Panels.LINKGRABBER, null);
                                JDGui.getInstance().setFrameState(FrameState.TO_FRONT_FOCUSED);
                            }
                        });
                        show(no);
                    }
                };
            }

            @Override
            public boolean isThisListenerEnabled() {
                return CFG_BUBBLE.CFG.isBubbleNotifyOnNewLinkgrabberLinksEnabled();
            }
        });

    }

    private void show(final AbstractNotifyWindow no) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ballooner.add(no);
            }
        };

    }

    @Override
    public void onReconnectSettingsUpdated(ReconnecterEvent event) {
    }

    @Override
    public void onUpdatesAvailable(boolean selfupdate, InstallLog installlog) {
        if (!CFG_BUBBLE.CFG.isBubbleNotifyOnUpdateAvailableEnabled()) return;
        if (UpdateController.getInstance().hasPendingUpdates() && !updatesNotified) {
            updatesNotified = true;
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    BasicNotify no = new BasicNotify(CFG_BUBBLE.BUBBLE_NOTIFY_ON_UPDATE_AVAILABLE_ENABLED, _GUI._.balloon_updates(), _GUI._.balloon_updates_msg(), NewTheme.I().getIcon("update", 32));
                    no.setActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            new UpdateAction(null).actionPerformed(e);
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
        if (!CFG_BUBBLE.CFG.isBubbleNotifyOnReconnectStartEnabled()) return;
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                BasicNotify no = new BasicNotify(CFG_BUBBLE.BUBBLE_NOTIFY_ON_RECONNECT_START_ENABLED, _GUI._.balloon_reconnect(), _GUI._.balloon_reconnect_start_msg(), NewTheme.I().getIcon("reconnect", 32));
                show(no);
            }
        };
    }

    @Override
    public void onAfterReconnect(final ReconnecterEvent event) {
        if (!CFG_BUBBLE.CFG.isBubbleNotifyOnReconnectEndEnabled()) return;
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                BasicNotify no = null;
                if (Boolean.FALSE.equals(event.getParameter())) {
                    no = new BasicNotify(CFG_BUBBLE.BUBBLE_NOTIFY_ON_RECONNECT_END_ENABLED, _GUI._.balloon_reconnect(), _GUI._.balloon_reconnect_end_msg_failed(IPController.getInstance().getIP()), NewTheme.I().getIcon("error", 32));
                } else {
                    no = new BasicNotify(CFG_BUBBLE.BUBBLE_NOTIFY_ON_RECONNECT_END_ENABLED, _GUI._.balloon_reconnect(), _GUI._.balloon_reconnect_end_msg(IPController.getInstance().getIP()), NewTheme.I().getIcon("ok", 32));
                }
                show(no);
            }
        };
    }
}
