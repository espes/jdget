package org.jdownloader.extensions.folderwatch;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JScrollPane;

import jd.controlling.JSonWrapper;
import jd.gui.UserIO;
import jd.gui.swing.jdgui.interfaces.SwitchPanel;
import jd.gui.swing.jdgui.views.InfoPanel;
import jd.gui.swing.jdgui.views.ViewToolbar;
import jd.nutils.JDFlags;
import net.miginfocom.swing.MigLayout;

import org.appwork.utils.swing.EDTHelper;
import org.jdownloader.actions.AppAction;
import org.jdownloader.extensions.folderwatch.data.History;
import org.jdownloader.extensions.folderwatch.data.HistoryEntry;
import org.jdownloader.extensions.folderwatch.translate.T;

public class FolderWatchPanel extends SwitchPanel {

    private static final long    serialVersionUID = -4451556977039313203L;

    private FolderWatchTable     table;
    private FolderWatchInfoPanel infoPanel;
    private JSonWrapper          config;

    private FolderWatchExtension owner;

    private AppAction            clearAction;

    private AppAction            addAction;

    public FolderWatchPanel(JSonWrapper jSonWrapper, FolderWatchExtension owner) {
        this.owner = owner;
        table = new FolderWatchTable(this);
        config = jSonWrapper;
        initActions();
        initGUI();
    }

    private void initGUI() {
        this.setLayout(new MigLayout("", "[]min[][grow,fill]min[grow, fill]"));
        this.add(new JScrollPane(table), "width max,wrap");
        this.add(new ViewToolbar(clearAction, addAction), "align center");
    }

    private void initActions() {
        clearAction = new AppAction() {
            {
                setName("Clear");
                setIconKey("clear");
            }

            public void actionPerformed(ActionEvent e) {
                new EDTHelper<Object>() {
                    @Override
                    public Object edtRun() {
                        if (JDFlags.hasSomeFlags(UserIO.getInstance().requestConfirmDialog(UserIO.NO_COUNTDOWN, T._.action_folderwatch_clear_message()), UserIO.RETURN_OK)) {
                            History.clear();
                            config.setProperty(FolderWatchConstants.PROPERTY_HISTORY, null);
                            config.save();
                            refresh();
                        }

                        return null;
                    }
                }.start();
            }

        };

        addAction = new AppAction() {
            {
                setName("Add");
                setIconKey("add");
            }

            public void actionPerformed(ActionEvent e) {
                new EDTHelper<Object>() {
                    @Override
                    public Object edtRun() {

                        if (table.getSelectedRowCount() > 0) {
                            int[] rows = table.getSelectedRows();

                            for (int row : rows) {
                                HistoryEntry container = (HistoryEntry) table.getValueAt(row, 2);
                                owner.importContainer(new File(container.getAbsolutePath()));
                            }
                        }
                        return null;
                    }
                }.start();
            }

        };
    }

    @Override
    protected void onHide() {
    }

    @Override
    protected void onShow() {
        refresh();
    }

    public FolderWatchInfoPanel getInfoPanel() {
        if (infoPanel == null) {
            infoPanel = new FolderWatchInfoPanel("unpack");
        }

        return infoPanel;
    }

    public void refresh() {
        table.getModel().refreshModel();
        table.getModel().fireTableDataChanged();

        getInfoPanel().update();
    }

    public class FolderWatchInfoPanel extends InfoPanel {

        private static final long serialVersionUID = -4944779193095436056L;

        public FolderWatchInfoPanel(String iconKey) {
            super(iconKey);

            addInfoEntry(T._.plugins_optional_folderwatch_panel_filestatus(), "", 0, 0);
        }

        public void update() {
            new EDTHelper<Object>() {
                @Override
                public Object edtRun() {
                    HistoryEntry container = (HistoryEntry) table.getValueAt(table.getSelectedRow(), 3);

                    String info = "";

                    if (container != null) {
                        container = History.updateEntry(container);

                        if (container.isExisting()) {
                            info = T._.plugins_optional_folderwatch_panel_filestatus_exists();
                        } else {
                            info = T._.plugins_optional_folderwatch_panel_filestatus_notexists();
                        }
                    }

                    updateInfo(T._.plugins_optional_folderwatch_panel_filestatus(), info);

                    return null;
                }
            }.start();
        }
    }

}