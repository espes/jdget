package org.jdownloader.gui.views.linkgrabber.bottombar;

import java.awt.event.ActionEvent;

import jd.controlling.ClipboardMonitoring;
import jd.controlling.ClipboardMonitoring.ClipboardContent;
import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkOrigin;
import jd.controlling.linkcrawler.LinkCrawler;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.controlling.contextmenu.ActionContext;
import org.jdownloader.controlling.contextmenu.CustomizableAppAction;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.linkgrabber.actions.AddLinksProgress;

public class PasteLinksAction extends CustomizableAppAction implements ActionContext {

    public static final String DEEP_DECRYPT_ENABLED = "deepDecryptEnabled";
    private boolean            deepDecryptEnabled   = false;

    @Customizer(name = "Deep Decrypt")
    public boolean isDeepDecryptEnabled() {

        return deepDecryptEnabled;
    }

    public void setDeepDecryptEnabled(boolean deepDecryptEnabled) {
        this.deepDecryptEnabled = deepDecryptEnabled;
        update();

    }

    private void update() {
        if (isDeepDecryptEnabled()) {
            setName(_GUI._.PasteLinksAction_PasteLinksAction_deep());

            setIconKey(IconKey.ICON_CLIPBOARD);
        } else {
            setName(_GUI._.PasteLinksAction_PasteLinksAction());

            setIconKey(IconKey.ICON_CLIPBOARD);
        }
    }

    public PasteLinksAction() {
        update();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ClipboardContent content = ClipboardMonitoring.getINSTANCE().getCurrentContent();
        final LinkCollectingJob crawljob = new LinkCollectingJob(LinkOrigin.PASTE_LINKS_ACTION, content != null ? content.getContent() : null);
        if (content != null) crawljob.setCustomSourceUrl(content.getBrowserURL());
        crawljob.setDeepAnalyse(isDeepDecryptEnabled());
        AddLinksProgress d = new AddLinksProgress(crawljob);
        if (d.isHiddenByDontShowAgain()) {
            Thread thread = new Thread("AddLinksDialog") {
                public void run() {
                    // we keep a reference to the text for later deep decrypt, because linkcollector clears the text from job
                    final String txt = crawljob.getText();
                    LinkCrawler lc = LinkCollector.getInstance().addCrawlerJob(crawljob);
                    if (lc != null) {
                        lc.waitForCrawling();
                        System.out.println("JOB DONE: " + lc.getCrawledLinksFoundCounter());
                        if (!crawljob.isDeepAnalyse() && lc.getProcessedLinksCounter() == 0 && lc.getUnhandledLinksFoundCounter() > 0) {
                            try {
                                Dialog.getInstance().showConfirmDialog(0, _GUI._.AddLinksAction_actionPerformed_deep_title(), _GUI._.AddLinksAction_actionPerformed_deep_msg(), null, _GUI._.literally_yes(), _GUI._.literall_no());
                                crawljob.setDeepAnalyse(true);
                                crawljob.setText(txt);
                                lc = LinkCollector.getInstance().addCrawlerJob(crawljob);
                                lc.waitForCrawling();
                                System.out.println("DEEP JOB DONE: " + lc.getCrawledLinksFoundCounter());
                            } catch (DialogClosedException e) {
                                e.printStackTrace();
                            } catch (DialogCanceledException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            };

            thread.start();
        } else {
            try {
                Dialog.getInstance().showDialog(d);
            } catch (DialogClosedException e1) {
                e1.printStackTrace();
            } catch (DialogCanceledException e1) {
                e1.printStackTrace();
            }
        }
    }

}
