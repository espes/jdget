package jd.gui.swing.jdgui.views.settings;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jd.SecondLevelLaunch;
import jd.gui.swing.jdgui.interfaces.SwitchPanel;
import jd.gui.swing.jdgui.views.settings.panels.GeneralSettingsConfigPanel;
import jd.gui.swing.jdgui.views.settings.sidebar.ConfigSidebar;
import net.miginfocom.swing.MigLayout;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.MigPanel;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;
import org.jdownloader.updatev2.gui.LAFOptions;

public class ConfigurationPanel extends SwitchPanel implements ListSelectionListener {

    private static final long              serialVersionUID = -6554600142198250742L;
    private ConfigSidebar                  sidebar;
    private SwitchPanel                    panel;
    private GraphicalUserInterfaceSettings cfg;
    private MigPanel                       right;
    private String                         lastE;

    public ConfigurationPanel() {
        super(new MigLayout("ins 0", "[][grow,fill]", "[grow,fill]"));
        sidebar = new ConfigSidebar();
        right = new RightPanel();

        // right.add(sb, "pushx,growx,pushy,growy");
        add(sidebar, "");
        JScrollPane sp;
        add(sp = new JScrollPane(right));
        sp.setBorder(null);
        // sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cfg = JsonConfig.create(GraphicalUserInterfaceSettings.class);
        // add(viewport);
        sidebar.addListener(this);
        Color c = (LAFOptions.getInstance().getColorForPanelBackground());

        if (c != null) {
            setBackground(c.brighter());
            super.setOpaque(true);
        }
        // sb.addComponentL7istener(this);
    }

    public void setOpaque(boolean isOpaque) {
    }

    @Override
    protected void onShow() {
        if (sidebar.treeInitiated() == false) SecondLevelLaunch.GUI_COMPLETE.executeWhenReached(new Runnable() {

            @Override
            public void run() {
                sidebar.updateAddons();
            }
        });
        new EDTRunner() {
            protected void runInEDT() {
                if (sidebar.getSelectedPanel() == null) {
                    sidebar.getTreeCompleteState().executeWhenReached(new Runnable() {

                        @Override
                        public void run() {
                            new EDTRunner() {

                                @Override
                                protected void runInEDT() {
                                    if (sidebar.getSelectedPanel() == null) {
                                        restoreSelection(true);
                                    }
                                }
                            };
                        }
                    });
                }
            };
        };

    }

    public void restoreSelection(final boolean onlyOnEmptySelection) {
        Class<?> selected = GeneralSettingsConfigPanel.class;
        try {
            String panelClass = cfg.getActiveConfigPanel();
            selected = Class.forName(panelClass);
        } catch (Throwable e) {

        }
        final Class<?> finalSelected = selected;
        if (finalSelected != null) {
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    if (onlyOnEmptySelection && sidebar.getSelectedPanel() != null) return;
                    sidebar.setSelectedTreeEntry(finalSelected);
                }
            };
        }
    }

    @Override
    protected void onHide() {

    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) { return; }

        if (lastE != null) {

            if (lastE.equals(e.toString())) {

            return; }
        }

        lastE = e.toString();
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                final SwitchPanel p = sidebar.getSelectedPanel();
                if (p == null) return;
                setContent(p);
            }
        };
        // invalidate();
    }

    @SuppressWarnings("rawtypes")
    private void setContent(SwitchPanel selectedPanel) {
        if (selectedPanel == null || selectedPanel == panel) return;

        if (panel != null) {
            panel.setHidden();
        }
        if (selectedPanel instanceof ExtensionConfigPanel) {
            cfg.setActiveConfigPanel(((ExtensionConfigPanel) selectedPanel).getExtension().getClass().getName());
        } else {
            cfg.setActiveConfigPanel(selectedPanel.getClass().getName());
        }

        // selectedPanel.setPreferredSize(sb.getSize());
        // sb.setViewportView(selectedPanel);
        right.removeAll();
        right.add(selectedPanel, "pushx,growx,pushy,growy");
        right.revalidate();
        // without the repaint, we get ugly glitches: http://svn.jdownloader.org/issues/8853
        right.repaint();
        panel = selectedPanel;
        panel.setShown();

    }

    // public boolean isOpaque() {
    // return true;
    // }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
    }

    public <T> T getSubPanel(Class<T> class1) {
        return (T) sidebar.getTreeEntry(class1);
    }

    public void setSelectedSubPanel(Class<?> class1) {
        sidebar.setSelectedTreeEntry(class1);
    }

}
