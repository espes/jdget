package jd.gui.swing.jdgui;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.JLabel;

import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import net.miginfocom.swing.MigLayout;

import org.appwork.swing.MigPanel;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.uio.CloseReason;
import org.appwork.uio.UIOManager;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.ConfirmDialog;
import org.appwork.utils.swing.dialog.DefaultButtonPanel;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.AbstractIcon;

public class OSRSurvey {
    private static final OSRSurvey INSTANCE = new OSRSurvey();

    /**
     * get the only existing instance of OSRSurvey. This is a singleton
     * 
     * @return
     */
    public static OSRSurvey getInstance() {
        return OSRSurvey.INSTANCE;
    }

    private AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Create a new instance of OSRSurvey. This is a singleton class. Access the only existing instance by using {@link #getInstance()}.
     */
    private OSRSurvey() {

    }

    public void start() {
        if (running.get()) return;
        running.set(true);
        try {

            try {
                Browser br = new Browser();
                br.openGetConnection("http://stats.appwork.org/piwik/piwik.php?idsite=6&rec=1&action_name=CALL_Dialog");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ConfirmDialog d = new ConfirmDialog(0, _GUI._.osr_dialog_title(), "", null, _GUI._.osr_start(), null) {
                private AbstractIcon header;
                {
                    header = new AbstractIcon("fau_osr_header", -1);
                }

                @Override
                protected int getPreferredWidth() {
                    return super.getPreferredWidth();
                }

                protected DefaultButtonPanel createBottomButtonPanel() {
                    // TODO Auto-generated method stub

                    return new DefaultButtonPanel("ins 0 0 0 5", "[]", "0[grow,fill]0");

                }

                @Override
                protected boolean isResizable() {
                    return false;
                }

                @Override
                public JComponent layoutDialogContent() {
                    this.getDialog().setLayout(new MigLayout("ins 0 0 5 0,wrap 1", "[]", "[][]"));
                    final MigPanel p = new MigPanel("ins 0,wrap 1", "[]", "[][]");
                    p.add(new JLabel(header), "");

                    JLabel lbl;
                    p.add(lbl = new JLabel(_GUI._.osr_dialog_message()) {
                        @Override
                        public Dimension getPreferredSize() {
                            return new Dimension(header.getIconWidth() - 10, super.getPreferredSize().height);
                        }
                    }, "gapleft 5,gapright 5");
                    lbl.addComponentListener(new ComponentListener() {

                        @Override
                        public void componentShown(ComponentEvent e) {
                        }

                        @Override
                        public void componentResized(ComponentEvent e) {
                            // setResizable(true);
                            getDialog().pack();

                            // setResizable(false);
                        }

                        @Override
                        public void componentMoved(ComponentEvent e) {
                        }

                        @Override
                        public void componentHidden(ComponentEvent e) {
                        }
                    });

                    return p;
                }

                @Override
                protected void packed() {

                    super.packed();

                }

                @Override
                public ModalityType getModalityType() {
                    return ModalityType.MODELESS;
                }
            };

            UIOManager.I().show(null, d);
            if (d.getCloseReason() == CloseReason.OK) {
                ArrayList<String> urls = new ArrayList<String>();

                String lng = ("de".equalsIgnoreCase(TranslationFactory.getDesiredLanguage()) ? "de" : "en");
                urls.add("http://osr-surveys.cs.fau.de/index.php/771316/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/583851/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/618667/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/621434/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/976745/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/815453/lang-" + lng);
                urls.add("http://osr-surveys.cs.fau.de/index.php/984994/lang-" + lng);

                String url;
                CrossSystem.openURLOrShowMessage(url = urls.get(new Random().nextInt(urls.size())));

                Browser br = new Browser();
                try {
                    br.openGetConnection("http://stats.appwork.org/piwik/piwik.php?idsite=6&rec=1&action_name=CALL_DialogOK");
                    br.openGetConnection("http://stats.appwork.org/piwik/piwik.php?idsite=6&rec=1&action_name=CALL_" + Encoding.urlEncode(url));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Browser br = new Browser();
                    br.openGetConnection("http://stats.appwork.org/piwik/piwik.php?idsite=6&rec=1&action_name=CALL_DialogCancel");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } finally {
            running.set(false);
        }
    }

}
