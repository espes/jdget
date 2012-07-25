package jd.gui.swing.jdgui.views.settings.panels.advanced;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jd.controlling.IOEQ;
import jd.gui.swing.jdgui.JDGui;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.swing.HelpNotifier;
import org.appwork.utils.swing.HelpNotifierCallbackListener;
import org.jdownloader.gui.settings.AbstractConfigPanel;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.settings.advanced.AdvancedConfigEventListener;
import org.jdownloader.settings.advanced.AdvancedConfigManager;
import org.jdownloader.translate._JDT;

public class AdvancedSettings extends AbstractConfigPanel implements DocumentListener, AdvancedConfigEventListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.jdownloader.gui.settings.AbstractConfigPanel#onShow()
     */
    @Override
    protected void onShow() {
        super.onShow();
        AdvancedConfigManager.getInstance().getEventSender().addListener(this);
        JDGui.help(_GUI._.AdvancedSettings_onShow_title_(), _GUI._.AdvancedSettings_onShow_msg_(), NewTheme.I().getIcon("warning", 32));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdownloader.gui.settings.AbstractConfigPanel#onHide()
     */
    @Override
    protected void onHide() {
        super.onHide();
        AdvancedConfigManager.getInstance().getEventSender().removeListener(this);
    }

    private static final long serialVersionUID = 1L;
    private JTextField        filterText;
    private String            filterHelp;
    private AdvancedTable     table;
    private DelayedRunnable   delayedRefresh;

    public String getTitle() {
        return _GUI._.gui_settings_advanced_title();
    }

    public AdvancedSettings() {
        super();
        this.addHeader(getTitle(), NewTheme.I().getIcon("advancedConfig", 32));
        this.addDescription(_JDT._.gui_settings_advanced_description());

        filterText = new JTextField() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                Composite comp = g2.getComposite();

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2.drawImage(NewTheme.I().getIcon("search", 16).getImage(), 3, 3, null);
                g2.setComposite(comp);
            }

        };

        HelpNotifier.register(filterText, new HelpNotifierCallbackListener() {

            public void onHelpNotifyShown(JComponent c) {
            }

            public void onHelpNotifyHidden(JComponent c) {
            }
        }, filterHelp = _GUI._.AdvancedSettings_AdvancedSettings_filter_());

        // filterText.setOpaque(false);
        // filterText.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        // filterText.setBorder(null);
        filterText.setBorder(BorderFactory.createCompoundBorder(filterText.getBorder(), BorderFactory.createEmptyBorder(0, 20, 0, 0)));
        add(filterText, "gapleft 37,spanx,growx,pushx");
        filterText.getDocument().addDocumentListener(this);
        add(new JScrollPane(table = new AdvancedTable()));
        delayedRefresh = new DelayedRunnable(IOEQ.TIMINGQUEUE, 200, 1000) {

            @Override
            public void delayedrun() {
                if (!filterText.getText().equals(filterHelp)) {
                    table.filter(filterText.getText());
                } else {
                    table.filter(null);
                }
            }

        };
    }

    @Override
    public ImageIcon getIcon() {
        return NewTheme.I().getIcon("advancedConfig", 20);
    }

    @Override
    public void save() {

    }

    @Override
    public void updateContents() {
        delayedRefresh.resetAndStart();
    }

    public void insertUpdate(DocumentEvent e) {
        delayedRefresh.resetAndStart();
    }

    public void removeUpdate(DocumentEvent e) {
        delayedRefresh.resetAndStart();
    }

    public void changedUpdate(DocumentEvent e) {
        delayedRefresh.resetAndStart();
    }

    public void onAdvancedConfigUpdate() {
        delayedRefresh.resetAndStart();
    }
}