package org.jdownloader.extensions.streaming.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jd.gui.swing.jdgui.interfaces.SwitchPanel;
import jd.gui.swing.laf.LookAndFeelController;
import jd.plugins.AddonPanel;
import net.miginfocom.swing.MigLayout;

import org.appwork.utils.logging.Log;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.extensions.streaming.StreamingExtension;
import org.jdownloader.extensions.streaming.gui.bottombar.BottomBar;
import org.jdownloader.extensions.streaming.gui.sidebar.Sidebar;
import org.jdownloader.extensions.streaming.gui.sidebar.SidebarHeader;
import org.jdownloader.gui.views.components.HeaderScrollPane;
import org.jdownloader.logging.LogController;

public class VLCGui extends AddonPanel<StreamingExtension> implements MouseListener {

    private static final String    ID = "VLCGUI";
    private SwitchPanel            panel;

    private LogSource              logger;
    private MediaArchiveTableModel model;
    private MediaArchiveTable      table;
    private JScrollPane            tableScrollPane;
    private BottomBar              bottomBar;
    private Sidebar                sidebar;
    private HeaderScrollPane       sidebarScrollPane;

    @SuppressWarnings("serial")
    public VLCGui(StreamingExtension plg) {
        super(plg);
        logger = LogController.getInstance().getLogger("VLCGUI");

        this.panel = new SwitchPanel(new MigLayout("ins 0, wrap 2", "[80!,grow,fill]2[grow,fill]", "[grow, fill]2[]")) {

            @Override
            protected void onShow() {

            }

            @Override
            protected void onHide() {
            }
        };

        model = new MediaArchiveTableModel(plg.getMediaArchiveController());
        table = new MediaArchiveTable(model);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(null);
        bottomBar = new BottomBar(plg, table);

        sidebar = new Sidebar(table);
        sidebarScrollPane = new HeaderScrollPane(sidebar) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

        };

        // ScrollPaneUI udi = sp.getUI();
        int c = LookAndFeelController.getInstance().getLAFOptions().getPanelBackgroundColor();
        // LayoutManager lm = sp.getLayout();

        if (c >= 0) {
            sidebarScrollPane.setBackground(new Color(c));
            sidebarScrollPane.setOpaque(true);

        }
        sidebarScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sidebarScrollPane.setColumnHeaderView(new SidebarHeader());

        panel.add(sidebarScrollPane);
        panel.add(tableScrollPane, "pushx,growx,spanx");
        //
        // }

        panel.add(bottomBar, "spanx,height 24!");
        // layout all contents in panel
        this.setContent(panel);

        layoutPanel();

    }

    private void layoutPanel() {

    }

    /**
     * is called if, and only if! the view has been closed
     */
    @Override
    protected void onDeactivated() {
        Log.L.finer("onDeactivated " + getClass().getSimpleName());
    }

    /**
     * is called, if the gui has been opened.
     */
    @Override
    protected void onActivated() {
        Log.L.finer("onActivated " + getClass().getSimpleName());
    }

    @Override
    public Icon getIcon() {
        return this.getExtension().getIcon(16);
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getTitle() {
        return getExtension()._.gui_title();
    }

    @Override
    public String getTooltip() {
        return getExtension()._.gui_tooltip();
    }

    /**
     * Is called if gui is visible now, and has not been visible before. For example, user starte the extension, opened the view, or
     * switched form a different tab to this one
     */
    @Override
    protected void onShow() {
        Log.L.finer("Shown " + getClass().getSimpleName());
    }

    /**
     * gets called of the extensiongui is not visible any more. for example because it has been closed or user switched to a different
     * tab/view
     */
    @Override
    protected void onHide() {
        Log.L.finer("hidden " + getClass().getSimpleName());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
