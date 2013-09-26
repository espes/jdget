package org.jdownloader.gui.views.linkgrabber;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinkCollectorCrawler;
import jd.controlling.linkcollector.LinkCollectorEvent;
import jd.controlling.linkcollector.LinkCollectorHighlightListener;
import jd.controlling.linkcollector.LinkCollectorListener;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.interfaces.SwitchPanel;
import net.miginfocom.swing.MigLayout;

import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.ExtButton;
import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.event.queue.Queue.QueuePriority;
import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.WindowManager.FrameState;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.components.OverviewHeaderScrollPane;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.components.HeaderScrollPane;
import org.jdownloader.gui.views.components.LinktablesSearchCategory;
import org.jdownloader.gui.views.components.packagetable.SearchField;
import org.jdownloader.gui.views.downloads.table.HorizontalScrollbarAction;
import org.jdownloader.gui.views.linkgrabber.actions.AddLinksAction;
import org.jdownloader.gui.views.linkgrabber.actions.AddOptionsAction;
import org.jdownloader.gui.views.linkgrabber.actions.ClearLinkgrabberAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmAllAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmOptionsAction;
import org.jdownloader.gui.views.linkgrabber.actions.RemoveOptionsAction;
import org.jdownloader.gui.views.linkgrabber.actions.ResetAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.LinkgrabberContextMenuManager;
import org.jdownloader.gui.views.linkgrabber.overview.LinkgrabberOverViewHeader;
import org.jdownloader.gui.views.linkgrabber.overview.LinkgrabberOverview;
import org.jdownloader.images.NewTheme;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;
import org.jdownloader.settings.GraphicalUserInterfaceSettings.NewLinksInLinkgrabberAction;
import org.jdownloader.settings.staticreferences.CFG_GUI;
import org.jdownloader.updatev2.gui.LAFOptions;

public class LinkGrabberPanel extends SwitchPanel implements LinkCollectorListener, GenericConfigEventListener<Boolean> {
    /**
     * 
     */
    private static final long     serialVersionUID = 1L;
    private LinkGrabberTableModel tableModel;
    private LinkGrabberTable      table;

    public LinkGrabberTable getTable() {
        return table;
    }

    private JScrollPane                                                        tableScrollPane;
    private LinkGrabberSidebar                                                 sidebar;
    private JButton                                                            addLinks;
    private JButton                                                            confirmAll;
    private JButton                                                            resetButton;
    private JButton                                                            popup;
    private JButton                                                            popupConfirm;
    private HeaderScrollPane                                                   sidebarScrollPane;
    private MigPanel                                                           leftBar;
    private MigPanel                                                           rightBar;
    private SearchField<LinktablesSearchCategory, CrawledPackage, CrawledLink> searchField;
    private ExtButton                                                          filteredAdd;

    private JButton                                                            popupRemove;
    private JToggleButton                                                      showHideSidebar;
    private AutoConfirmButton                                                  autoConfirm;
    private NullsafeAtomicReference<OverviewHeaderScrollPane>                  overViewScrollBar = new NullsafeAtomicReference<OverviewHeaderScrollPane>(null);
    private JToggleButton                                                      bottomBar;

    public LinkGrabberPanel() {
        super(new MigLayout("ins 0, wrap 2", "[grow,fill]2[]2[fill]", "[grow, fill]2[]"));

        tableModel = LinkGrabberTableModel.getInstance();
        table = new LinkGrabberTable(this, tableModel);
        tableScrollPane = new JScrollPane(table);
        HorizontalScrollbarAction.setup(CFG_GUI.HORIZONTAL_SCROLLBARS_IN_LINKGRABBER_TABLE_ENABLED, table);
        filteredAdd = new ExtButton(new AppAction() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {
                setIconKey("filter");
            }

            public void actionPerformed(ActionEvent e) {
                LinkCollector.getInstance().getQueue().add(new QueueAction<Void, RuntimeException>() {

                    @Override
                    protected Void run() throws RuntimeException {
                        java.util.List<CrawledLink> filteredStuff = LinkCollector.getInstance().getFilteredStuff(true);
                        LinkCollector.getInstance().addCrawlerJob(filteredStuff);
                        return null;
                    }
                });
            }

        });

        // filteredAdd.setVisible(false);
        LinkCollector.getInstance().getEventsender().addListener(new LinkCollectorHighlightListener() {

            public void onLinkCollectorFilteredLinksAvailable(LinkCollectorEvent event) {
                setFilteredAvailable(LinkCollector.getInstance().getfilteredStuffSize());
            }

            public void onLinkCollectorFilteredLinksEmpty(LinkCollectorEvent event) {
                setFilteredAvailable(0);
            }

            @Override
            public void onHighLight(CrawledLink parameter) {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        try {
                            System.out.println("Highlight");
                            if (CFG_GUI.CFG.isSwitchToLinkgrabberTabOnNewLinksAddedEnabled()) JDGui.getInstance().requestPanel(JDGui.Panels.LINKGRABBER);

                            switch (CFG_GUI.CFG.getNewLinksAction()) {
                            case FOCUS:
                                JDGui.getInstance().setFrameState(FrameState.TO_FRONT_FOCUSED);
                                break;
                            case NOTHING:
                                JDGui.getInstance().flashTaskbar();
                                return;
                            case TO_FRONT:
                                JDGui.getInstance().setFrameState(FrameState.TO_FRONT);
                                break;
                            }
                        } catch (Throwable e) {
                        }
                    }
                };

            }

            @Override
            public boolean isThisListenerEnabled() {
                return org.jdownloader.settings.staticreferences.CFG_GUI.CFG.isLinkgrabberAutoTabSwitchEnabled() || CFG_GUI.CFG.getNewLinksAction() != NewLinksInLinkgrabberAction.NOTHING;
            }

            @Override
            public void onLinkCrawlerAdded(LinkCollectorCrawler parameter) {
            }

            @Override
            public void onLinkCrawlerStarted(LinkCollectorCrawler parameter) {
            }

            @Override
            public void onLinkCrawlerStopped(LinkCollectorCrawler parameter) {
            }

        });

        autoConfirm = new AutoConfirmButton();

        setFilteredAvailable(LinkCollector.getInstance().getfilteredStuffSize());
        addLinks = new ExtButton(new AddLinksAction());
        confirmAll = new ExtButton(new ConfirmAllAction() {
            {
                org.jdownloader.settings.staticreferences.CFG_LINKFILTER.LINKGRABBER_AUTO_START_ENABLED.getEventSender().addListener(new GenericConfigEventListener<Boolean>() {

                    @Override
                    public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                        if (newValue != null) setAutoStart(newValue);
                    }

                    @Override
                    public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                    }
                }, false);
                setAutoStart(org.jdownloader.settings.staticreferences.CFG_LINKFILTER.LINKGRABBER_AUTO_START_ENABLED.isEnabled());

            }

        });
        switch (CFG_GUI.CFG.getLinkgrabberDefaultClearAction()) {

        case RESET_PANEL:
            resetButton = new JButton(new ResetAction(null));
            break;
        default:
            resetButton = new JButton(new ClearLinkgrabberAction());
        }

        popup = new JButton(new AddOptionsAction(addLinks)) {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x - 2, y, width + 2, height);
            }
        };
        popupRemove = new JButton(new RemoveOptionsAction(this, resetButton)) {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x - 2, y, width + 2, height);
            }
        };

        popupConfirm = new JButton(new ConfirmOptionsAction(table, confirmAll)) {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x - 2, y, width + 2, height);
            }

        };

        showHideSidebar = new JToggleButton(new AppAction() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {

                putValue(SMALL_ICON, NewTheme.I().getIcon("sidebar", -1));
                setTooltipText(_GUI._.LinkGrabberPanel_LinkGrabberPanel_btn_showsidebar_tt_up());

            }

            public void actionPerformed(ActionEvent e) {
                org.jdownloader.settings.staticreferences.CFG_GUI.CFG.setLinkgrabberSidebarVisible(!org.jdownloader.settings.staticreferences.CFG_GUI.CFG.isLinkgrabberSidebarVisible());
            }
        });
        showHideSidebar.setSelected(org.jdownloader.settings.staticreferences.CFG_GUI.CFG.isLinkgrabberSidebarVisible());
        leftBar = new MigPanel("ins 0", "[]1[]3[]1[]3[grow,fill]0[]", "[]");
        rightBar = new MigPanel("ins 0,debug", "[]0[]1[]0[]0", "[]");

        leftBar.add(addLinks, "height 24!,aligny top");

        leftBar.add(popup, "height 24!,width 12!,aligny top");
        leftBar.add(resetButton, "width 24!,height 24!,aligny top");
        leftBar.add(popupRemove, "height 24!,width 12!,aligny top");
        bottomBar = new JToggleButton(new AppAction() {
            {
                setIconKey("bottombar");
                setSelected(CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.isEnabled());

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.toggle();
            }
        });

        searchField = new SearchField<LinktablesSearchCategory, CrawledPackage, CrawledLink>(table, LinktablesSearchCategory.FILENAME) {
            @Override
            public void setSelectedCategory(LinktablesSearchCategory selectedCategory) {
                super.setSelectedCategory(selectedCategory);
                JsonConfig.create(GraphicalUserInterfaceSettings.class).setSelectedLinkgrabberSearchCategory(selectedCategory);
            }

            @Override
            public boolean isFiltered(CrawledPackage e) {
                if (LinktablesSearchCategory.PACKAGE == selectedCategory) {

                    for (Pattern filterPattern : filterPatterns) {
                        if (filterPattern.matcher(e.getName()).find()) return false;
                    }
                    return true;

                }
                return false;
            }

            @Override
            public boolean isFiltered(CrawledLink v) {

                switch (selectedCategory) {
                case FILENAME:
                    for (Pattern filterPattern : filterPatterns) {
                        if (filterPattern.matcher(v.getName()).find()) return false;
                    }
                    return true;
                case HOSTER:
                    for (Pattern filterPattern : filterPatterns) {
                        if (filterPattern.matcher(v.getHost()).find()) return false;
                    }
                    return true;
                }
                return false;

            }

        };
        searchField.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchField.setText("");

                }
            }

            public void keyPressed(KeyEvent e) {
            }
        });
        searchField.setSelectedCategory(JsonConfig.create(GraphicalUserInterfaceSettings.class).getSelectedLinkgrabberSearchCategory());
        searchField.setCategories(new LinktablesSearchCategory[] { LinktablesSearchCategory.FILENAME, LinktablesSearchCategory.HOSTER, LinktablesSearchCategory.PACKAGE });
        leftBar.add(searchField, "height 24!,aligny top");

        leftBar.add(filteredAdd, "height 24!,hidemode 3,gapleft 4");
        // leftBar.add(Box.createGlue());
        layoutComponents();

        // showHideSidebar.setVisible(org.jdownloader.settings.statics.GUI.LINKGRABBER_SIDEBAR_ENABLED.getValue());
        org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_ENABLED.getEventSender().addListener(this);

        org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_TOGGLE_BUTTON_ENABLED.getEventSender().addListener(this);
        org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_VISIBLE.getEventSender().addListener(this);
        org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.getEventSender().addListener(this);
        LinkgrabberContextMenuManager.getInstance().setPanel(this);

    }

    public SearchField<LinktablesSearchCategory, CrawledPackage, CrawledLink> getSearchField() {
        return searchField;
    }

    private void setFilteredAvailable(final int size) {
        if (size > 0) {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    filteredAdd.setText(_GUI._.RestoreFilteredLinksAction_(size));
                    filteredAdd.setVisible(true);
                }
            };
        } else {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    filteredAdd.setVisible(false);
                }
            };
        }
    }

    private Component getOverView() {
        OverviewHeaderScrollPane ret = overViewScrollBar.get();
        if (ret != null) {
            return ret;
        } else {
            final LinkgrabberOverview loverView = new LinkgrabberOverview(table) {
                @Override
                public void removeListeners() {
                    super.removeListeners();
                    overViewScrollBar.set(null);
                }
            };
            ret = new OverviewHeaderScrollPane(loverView);
            final OverviewHeaderScrollPane finalRet = ret;
            LAFOptions.getInstance().applyPanelBackground(ret);
            ret.setColumnHeaderView(new LinkgrabberOverViewHeader() {

                @Override
                protected void onCloseAction() {
                    CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.setValue(false);
                    loverView.removeListeners();
                }
            });
            overViewScrollBar.compareAndSet(null, ret);
        }
        return ret;
    }

    private void layoutComponents() {
        rightBar.removeAll();

        boolean showSidebarToggle = org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_TOGGLE_BUTTON_ENABLED.getValue() && org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_ENABLED.getValue();
        if (showSidebarToggle) {

            rightBar.setLayout(new MigLayout("ins 0", "[]0[]1[]0[]0[]0", "[]"));
        } else {
            rightBar.setLayout(new MigLayout("ins 0", "[]0[]1[]0[]0", "[]"));
        }

        rightBar.add(autoConfirm, "height 24!,width 24!,hidemode 3,gapright 3");
        autoConfirm.setVisible(false);
        rightBar.add(confirmAll, "height 24!,pushx,growx");
        rightBar.add(popupConfirm, "height 24!,width 12!");
        rightBar.add(bottomBar, "height 24!,width 24!,gapleft 1");
        if (showSidebarToggle) {
            //
            rightBar.add(showHideSidebar, "height 24!,width 24!,gapleft 2");

        }

        if (org.jdownloader.settings.staticreferences.CFG_GUI.CFG.isLinkgrabberSidebarEnabled() && org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_VISIBLE.getValue()) {

            if (sidebarScrollPane == null) {
                createSidebar();
            }

            if (CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.isEnabled()) {
                this.add(tableScrollPane, "");
                add(sidebarScrollPane, "spany 2,growx");
                add(getOverView());

            } else {
                this.add(tableScrollPane, "spany 2");
                add(sidebarScrollPane, "spany 2,growx");
            }

        } else {

            if (CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.isEnabled()) {

                this.add(tableScrollPane, "spanx");
                add(getOverView(), "spanx");
            } else {
                this.add(tableScrollPane, "spany 2,spanx,growx");
            }

        }

        add(leftBar);
        add(rightBar, "growx");
    }

    private void createSidebar() {
        sidebar = new LinkGrabberSidebar(table);

        sidebarScrollPane = new HeaderScrollPane(sidebar) {
            {
                getVerticalScrollBar().setUnitIncrement(24);
            }

            public Dimension getPreferredSize() {
                Dimension ret = super.getPreferredSize();
                Insets borderInsets = getBorder().getBorderInsets(sidebarScrollPane);
                ret.width = sidebar.getPreferredSize().width + getVerticalScrollBar().getPreferredSize().width + borderInsets.left + borderInsets.right;

                return ret;
            }

            public Dimension getMinimumSize() {
                Dimension pref = getPreferredSize();
                pref.height = 0;
                return pref;
            }

            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            // protected int getHeaderHeight() {
            // return (int)
            // table.getTableHeader().getPreferredSize().getHeight();
            // }
        };

        // ScrollPaneUI udi = sp.getUI();

        LAFOptions.getInstance().applyPanelBackground(sidebarScrollPane);
        sidebarScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScrollPane.setColumnHeaderView(new LinkGrabberSideBarHeader(table));
        // ExtButton bt = new ExtButton(new AppAction() {
        // {
        // setSmallIcon(NewTheme.I().getIcon("close", -1));
        // setToolTipText(_GUI._.LinkGrabberSideBarHeader_LinkGrabberSideBarHeader_object_());
        // }
        //
        // public void actionPerformed(ActionEvent e) {
        // org.jdownloader.settings.statics.GUI.LINKGRABBER_SIDEBAR_ENABLED.setValue(false);
        // }
        // });
        //
        // sidebarScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER,
        // bt);
        org.jdownloader.settings.staticreferences.CFG_LINKFILTER.LINKGRABBER_QUICK_SETTINGS_VISIBLE.getEventSender().addListener(new GenericConfigEventListener<Boolean>() {

            public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
            }

            public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {

                if (Boolean.TRUE.equals(newValue)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            sidebarScrollPane.getVerticalScrollBar().setValue(sidebarScrollPane.getVerticalScrollBar().getMaximum());
                        }
                    });

                }
            }
        });

    }

    @Override
    protected void onShow() {
        tableModel.recreateModel(false);
        LinkCollector.getInstance().getEventsender().addListener(this);
        table.requestFocusInWindow();
    }

    @Override
    protected void onHide() {
        LinkCollector.getInstance().getEventsender().removeListener(this);
    }

    public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
    }

    public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
        if (!newValue && keyHandler == org.jdownloader.settings.staticreferences.CFG_GUI.LINKGRABBER_SIDEBAR_VISIBLE) {
            JDGui.help(_GUI._.LinkGrabberPanel_onConfigValueModified_title_(), _GUI._.LinkGrabberPanel_onConfigValueModified_msg_(), NewTheme.I().getIcon("warning_green", 32));
        }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                bottomBar.setSelected(CFG_GUI.LINKGRABBER_OVERVIEW_VISIBLE.isEnabled());
                removeAll();
                layoutComponents();

                revalidate();
            }
        };
    }

    public void onLinkCollectorAbort(LinkCollectorEvent event) {
    }

    public void onLinkCollectorFilteredLinksAvailable(LinkCollectorEvent event) {
    }

    public void onLinkCollectorFilteredLinksEmpty(LinkCollectorEvent event) {
    }

    public void onLinkCollectorDataRefresh(LinkCollectorEvent event) {
        tableModel.refreshModel(!QueuePriority.HIGH.equals(event.getPrio()));
    }

    public void onLinkCollectorStructureRefresh(LinkCollectorEvent event) {
        tableModel.recreateModel(!QueuePriority.HIGH.equals(event.getPrio()));
    }

    public void onLinkCollectorLinksRemoved(LinkCollectorEvent event) {
        tableModel.recreateModel(!QueuePriority.HIGH.equals(event.getPrio()));
    }

    public void onLinkCollectorLinkAdded(LinkCollectorEvent event, CrawledLink parameter) {
    }

    @Override
    public void onLinkCollectorDupeAdded(LinkCollectorEvent event, CrawledLink parameter) {
    }

    public void resetSearch() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                searchField.setText("");
                searchField.onChanged();
            }
        };
    }

    public void onLinkCollectorContentRemoved(LinkCollectorEvent event) {
    }

    public void onLinkCollectorContentAdded(LinkCollectorEvent event) {
    }

    public void onLinkCollectorContentModified(LinkCollectorEvent event) {
    }

    @Override
    public void onLinkCrawlerAdded(LinkCollectorCrawler parameter) {
    }

    @Override
    public void onLinkCrawlerStarted(LinkCollectorCrawler parameter) {
    }

    @Override
    public void onLinkCrawlerStopped(LinkCollectorCrawler parameter) {
    }

}
