package org.jdownloader.gui.views.linkgrabber.contextmenu;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.interfaces.View;

import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.annotations.EnumLabel;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.ConfirmDialog;
import org.appwork.utils.swing.dialog.DefaultButtonPanel;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.contextmenu.ActionContext;
import org.jdownloader.controlling.contextmenu.CustomizableSelectionAppAction;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.extraction.Archive;
import org.jdownloader.extensions.extraction.DummyArchive;
import org.jdownloader.extensions.extraction.ExtractionExtension;
import org.jdownloader.extensions.extraction.ValidateArchiveAction;
import org.jdownloader.extensions.extraction.gui.DummyArchiveDialog;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.KeyObserver;
import org.jdownloader.gui.event.GUIEventSender;
import org.jdownloader.gui.event.GUIListener;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.linkgrabber.addlinksdialog.LinkgrabberSettings;
import org.jdownloader.images.NewTheme;

public class ConfirmSelectionContextAction extends CustomizableSelectionAppAction<CrawledPackage, CrawledLink> implements GUIListener, ActionContext {

    /**
     * 
     */
    private static final long serialVersionUID = -3937346180905569896L;

    public static enum AutoStartOptions {
        @EnumLabel("Autostart: Never start Downloads")
        DISABLED,
        @EnumLabel("Autostart: Always start Downloads")
        ENABLED,
        @EnumLabel("Autostart: Automode (Quicksettings)")
        AUTO

    }

    private AutoStartOptions autoStart = AutoStartOptions.AUTO;

    public AutoStartOptions getAutoStart() {
        return autoStart;
    }

    public static final String AUTO_START = "autoStart";

    @Customizer(name = "Autostart Downloads afterwards")
    public ConfirmSelectionContextAction setAutoStart(AutoStartOptions autoStart) {
        if (autoStart == null) autoStart = AutoStartOptions.AUTO;
        this.autoStart = autoStart;

        updateLabelAndIcon();
        return this;
    }

    private void updateLabelAndIcon() {
        if (doAutostart()) {
            setName(_GUI._.ConfirmAction_ConfirmAction_context_add_and_start());
            Image add = NewTheme.I().getImage("media-playback-start", 16);
            Image play = NewTheme.I().getImage("add", 14);
            setSmallIcon(new ImageIcon(ImageProvider.merge(add, play, 0, 0, 6, 6)));
            setIconKey(null);
        } else {
            setName(_GUI._.ConfirmAction_ConfirmAction_context_add());
            setSmallIcon(NewTheme.I().getIcon(IconKey.ICON_GO_NEXT, 20));
        }
    }

    protected boolean doAutostart() {
        boolean ret = autoStart == AutoStartOptions.ENABLED || (autoStart == AutoStartOptions.AUTO && org.jdownloader.settings.staticreferences.CFG_LINKGRABBER.LINKGRABBER_AUTO_START_ENABLED.getValue());
        if (metaCtrl) {
            ret = !ret;
        }
        return ret;
    }

    protected static void switchToDownloadTab() {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                JDGui.getInstance().requestPanel(JDGui.Panels.DOWNLOADLIST);
            }
        };

    }

    private boolean clearListAfterConfirm = false;
    private boolean metaCtrl              = false;

    @Customizer(name = "Clear Linkgrabber after adding links")
    public boolean isClearListAfterConfirm() {
        return clearListAfterConfirm;
    }

    @Customizer(name = "Clear Linkgrabber after adding links")
    public void setClearListAfterConfirm(boolean clearListAfterConfirm) {
        this.clearListAfterConfirm = clearListAfterConfirm;
    }

    public ConfirmSelectionContextAction() {
        super();

        GUIEventSender.getInstance().addListener(this, true);
        metaCtrl = KeyObserver.getInstance().isMetaDown() || KeyObserver.getInstance().isControlDown();

    }

    @Override
    protected void initContextDefaults() {
        setAutoStart(AutoStartOptions.AUTO);
    }

    public ConfirmSelectionContextAction(SelectionInfo<CrawledPackage, CrawledLink> selectionInfo) {
        this();
        selection = selectionInfo;
    }

    @Override
    public void requestUpdate(Object requestor) {
        super.requestUpdate(requestor);
        updateLabelAndIcon();
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        confirmSelection(getSelection(), doAutostart(), isClearListAfterConfirm(), JsonConfig.create(LinkgrabberSettings.class).isAutoSwitchToDownloadTableOnConfirmDefaultEnabled());

    }

    public static void confirmSelection(final SelectionInfo<CrawledPackage, CrawledLink> selection, final boolean autoStart, final boolean clearLinkgrabber, final boolean doTabSwitch) {
        Thread thread = new Thread() {

            public void run() {
                try {
                    // this validation step also copies the passwords from the CRawledlinks in the archive settings
                    ValidateArchiveAction<CrawledPackage, CrawledLink> va = new ValidateArchiveAction<CrawledPackage, CrawledLink>((ExtractionExtension) ExtensionController.getInstance().getExtension(ExtractionExtension.class)._getExtension(), selection);
                    for (Archive a : va.getArchives()) {
                        final DummyArchive da = va.createDummyArchive(a);
                        if (!da.isComplete()) {

                            ConfirmDialog d = new ConfirmDialog(0, _GUI._.ConfirmAction_run_incomplete_archive_title_(a.getName()), _GUI._.ConfirmAction_run_incomplete_archive_msg(), NewTheme.I().getIcon("stop", 32), _GUI._.ConfirmAction_run_incomplete_archive_continue(), null) {
                                protected DefaultButtonPanel createBottomButtonPanel() {

                                    DefaultButtonPanel ret = new DefaultButtonPanel("ins 0", "[]", "0[]0");
                                    ret.add(new JButton(new AppAction() {
                                        {
                                            setName(_GUI._.ConfirmAction_run_incomplete_archive_details());
                                        }

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            try {
                                                Dialog.getInstance().showDialog(new DummyArchiveDialog(da));
                                            } catch (DialogClosedException e1) {
                                                e1.printStackTrace();
                                            } catch (DialogCanceledException e1) {
                                                e1.printStackTrace();
                                            }
                                        }

                                    }), "gapleft 32");
                                    return ret;
                                }

                            };

                            Dialog.getInstance().showDialog(d);
                        }

                    }

                } catch (DialogNoAnswerException e) {
                    return;
                } catch (Throwable e) {
                    Log.exception(e);
                }

                LinkCollector.getInstance().moveLinksToDownloadList(selection);
                if (autoStart) {
                    DownloadWatchDog.getInstance().startDownloads();
                }
                if (doTabSwitch) switchToDownloadTab();

                if (clearLinkgrabber) {
                    LinkCollector.getInstance().getQueue().add(new QueueAction<Void, RuntimeException>() {

                        @Override
                        protected Void run() throws RuntimeException {
                            LinkCollector.getInstance().clear();
                            return null;
                        }
                    });
                }
            }

        };
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName(ConfirmSelectionContextAction.class.getName());
        thread.start();
    }

    @Override
    public boolean isEnabled() {
        return hasSelection();
    }

    @Override
    public void onGuiMainTabSwitch(View oldView, View newView) {
    }

    @Override
    public void onKeyModifier(int parameter) {
        if (KeyObserver.getInstance().isControlDown() || KeyObserver.getInstance().isMetaDown()) {
            metaCtrl = true;
        } else {
            metaCtrl = false;
        }

        updateLabelAndIcon();
    }
}
