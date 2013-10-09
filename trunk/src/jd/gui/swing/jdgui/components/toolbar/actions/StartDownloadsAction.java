package jd.gui.swing.jdgui.components.toolbar.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import jd.controlling.TaskQueue;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.event.DownloadWatchdogListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.JDGui.Panels;
import jd.gui.swing.jdgui.MainTabbedPane;
import jd.gui.swing.jdgui.interfaces.View;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.gui.event.GUIEventSender;
import org.jdownloader.gui.event.GUIListener;
import org.jdownloader.gui.toolbar.action.ToolBarAction;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTableModel;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberView;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmAutoAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmAutoAction.AutoStartOptions;
import org.jdownloader.settings.GraphicalUserInterfaceSettings.StartButtonAction;
import org.jdownloader.settings.staticreferences.CFG_GUI;
import org.jdownloader.translate._JDT;

public class StartDownloadsAction extends ToolBarAction implements DownloadWatchdogListener, GUIListener, GenericConfigEventListener<Enum> {

    /**
     * Create a new instance of StartDownloadsAction. This is a singleton class. Access the only existing instance by using
     * {@link #getInstance()}.
     */
    public StartDownloadsAction() {

        setIconKey("media-playback-start");
        setName(_JDT._.StartDownloadsAction_createTooltip_());
        DownloadWatchDog.getInstance().getEventSender().addListener(this, true);
        DownloadWatchDog.getInstance().notifyCurrentState(this);
        CFG_GUI.START_BUTTON_ACTION_IN_LINKGRABBER_CONTEXT.getEventSender().addListener(this, true);
        GUIEventSender.getInstance().addListener(this, true);
        onGuiMainTabSwitch(null, MainTabbedPane.getInstance().getSelectedView());
        setHideIfDownloadsAreRunning(false);
        setAccelerator(KeyEvent.VK_S);
    }

    public void actionPerformed(final ActionEvent e) {
        if (JDGui.getInstance().isCurrentPanel(Panels.LINKGRABBER)) {
            TaskQueue.getQueue().add(new QueueAction<Void, RuntimeException>() {

                @Override
                protected Void run() throws RuntimeException {
                    switch (CFG_GUI.CFG.getStartButtonActionInLinkgrabberContext()) {
                    case ADD_ALL_LINKS_AND_START_DOWNLOADS:
                        java.util.List<AbstractNode> packages = new ArrayList<AbstractNode>(LinkGrabberTableModel.getInstance().getAllPackageNodes());
                        ConfirmAutoAction ca = new ConfirmAutoAction(new SelectionInfo<CrawledPackage, CrawledLink>(null, packages, null, null, e, LinkGrabberTableModel.getInstance().getTable()));
                        ca.setAutoStart(AutoStartOptions.ENABLED);
                        ca.actionPerformed(null);
                        break;
                    case START_DOWNLOADS_ONLY:
                        DownloadWatchDog.getInstance().startDownloads();
                        break;
                    }
                    return null;
                }
            });
        } else {
            DownloadWatchDog.getInstance().startDownloads();
        }
    }

    @Override
    public String createTooltip() {
        return _JDT._.StartDownloadsAction_createTooltip_();
    }

    private boolean            hideIfDownloadsAreRunning     = false;

    public static final String HIDE_IF_DOWNLOADS_ARE_RUNNING = "HideIfDownloadsAreRunning";

    @Customizer(name = "Hide if downloads are not running")
    public boolean isHideIfDownloadsAreRunning() {
        return hideIfDownloadsAreRunning;
    }

    public void setHideIfDownloadsAreRunning(boolean showIfDownloadsAreRunning) {
        this.hideIfDownloadsAreRunning = showIfDownloadsAreRunning;

        if (isHideIfDownloadsAreRunning() && DownloadWatchDog.getInstance().isRunning()) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }

    @Override
    public void onDownloadWatchdogDataUpdate() {
    }

    @Override
    public void onDownloadWatchdogStateIsIdle() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                setEnabled(true);
            }
        };

    }

    @Override
    public void onDownloadWatchdogStateIsPause() {

    }

    @Override
    public void onDownloadWatchdogStateIsRunning() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                setEnabled(false);
                if (isHideIfDownloadsAreRunning()) {
                    setVisible(false);

                }
            }
        };
    }

    @Override
    public void onDownloadWatchdogStateIsStopped() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                setEnabled(true);
                setVisible(true);
            }
        };
    }

    @Override
    public void onDownloadWatchdogStateIsStopping() {
    }

    @Override
    public void onGuiMainTabSwitch(View oldView, final View newView) {
        if (newView instanceof LinkGrabberView && CFG_GUI.CFG.getStartButtonActionInLinkgrabberContext() == StartButtonAction.DISABLED) {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    setEnabled(false);
                }
            };
        } else {
            DownloadWatchDog.getInstance().notifyCurrentState(this);
        }
    }

    @Override
    public void onConfigValidatorError(KeyHandler<Enum> keyHandler, Enum invalidValue, ValidationException validateException) {
    }

    @Override
    public void onConfigValueModified(KeyHandler<Enum> keyHandler, Enum newValue) {
        onGuiMainTabSwitch(null, MainTabbedPane.getInstance().getSelectedView());
    }
}
