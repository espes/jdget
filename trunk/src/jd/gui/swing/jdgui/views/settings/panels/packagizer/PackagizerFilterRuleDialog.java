package jd.gui.swing.jdgui.views.settings.panels.packagizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.JTextComponent;

import jd.controlling.linkcrawler.CrawledLink;
import jd.gui.swing.jdgui.events.EDTEventQueue;
import jd.gui.swing.jdgui.views.settings.panels.linkgrabberfilter.editdialog.ConditionDialog;
import jd.gui.swing.jdgui.views.settings.panels.linkgrabberfilter.editdialog.FilterPanel;
import jd.gui.swing.jdgui.views.settings.panels.linkgrabberfilter.test.TestWaitDialog;
import jd.gui.swing.jdgui.views.settings.panels.packagizer.test.PackagizerSingleTestTableModel;
import jd.gui.swing.laf.LookAndFeelController;

import org.appwork.app.gui.MigPanel;
import org.appwork.app.gui.copycutpaste.CopyAction;
import org.appwork.app.gui.copycutpaste.CutAction;
import org.appwork.app.gui.copycutpaste.DeleteAction;
import org.appwork.app.gui.copycutpaste.PasteAction;
import org.appwork.app.gui.copycutpaste.SelectAction;
import org.appwork.swing.components.ExtCheckBox;
import org.appwork.swing.components.ExtSpinner;
import org.appwork.swing.components.ExtTextField;
import org.appwork.swing.components.pathchooser.PathChooser;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.StringUtils;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.controlling.Priority;
import org.jdownloader.controlling.packagizer.PackagizerController;
import org.jdownloader.controlling.packagizer.PackagizerRule;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class PackagizerFilterRuleDialog extends ConditionDialog<PackagizerRule> {
    public Priority prio = Priority.DEFAULT;

    private class PriorityAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Priority          priority;

        public PriorityAction(Priority priority) {

            this.priority = priority;

        }

        public void actionPerformed(ActionEvent e) {
            prio = priority;
        }

        public ImageIcon getIcon() {
            return priority.loadIcon(18);
        }

        public String getTooltipText() {
            return priority._();
        }

    }

    private class RadioButton extends JRadioButton {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public RadioButton(PriorityAction pa_1) {
            super(pa_1);
            setToolTipText(pa_1.getTooltipText());
        }

    }

    private PackagizerRule rule;
    private JLabel         lblDest;
    private JLabel         lblPriority;
    private JLabel         lblPackagename;
    private JLabel         lblExtract;
    private JLabel         lblChunks;
    private JComboBox      cobExtract;
    private JComboBox      cobAutostart;
    private JComboBox      cobAutoAdd;

    protected void runTest(String text) {

        TestWaitDialog d;
        try {

            PackagizerController packagizer = PackagizerController.createEmptyTestInstance();
            final PackagizerRule rule = getCurrentCopy();
            packagizer.add(rule);
            d = new TestWaitDialog(text, _GUI._.PackagizerRuleDialog_runTest_title_(rule.toString()), null) {

                @Override
                protected ExtTableModel<CrawledLink> createTableModel() {
                    return new PackagizerSingleTestTableModel(rule);
                }

            };
            d.setPackagizer(packagizer);
            ArrayList<CrawledLink> ret = Dialog.getInstance().showDialog(d);
        } catch (DialogClosedException e) {
            e.printStackTrace();
        } catch (DialogCanceledException e) {
            e.printStackTrace();
        }
    }

    public PackagizerFilterRuleDialog(PackagizerRule filterRule) {
        super();
        this.rule = filterRule;

    }

    /**
     * Returns a Linkgrabberfilter representing current settings. does NOT save
     * the original one
     * 
     * @return
     */
    private PackagizerRule getCurrentCopy() {

        PackagizerRule ret = this.rule.duplicate();
        save(ret);
        return ret;
    }

    @Override
    protected PackagizerRule createReturnValue() {
        return rule;
    }

    public static void main(String[] args) {
        try {
            EDTEventQueue.initEventQueue();
            LookAndFeelController.getInstance().setUIManager();
            Dialog.getInstance().showDialog(new PackagizerFilterRuleDialog(new PackagizerRule()));
        } catch (DialogClosedException e) {
            e.printStackTrace();
        } catch (DialogCanceledException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    @Override
    protected void setReturnmask(boolean b) {
        super.setReturnmask(b);
        if (b) {
            save(rule);
        }
    }

    private void save(PackagizerRule rule) {
        rule.setFilenameFilter(getFilenameFilter());
        rule.setHosterURLFilter(getHosterFilter());
        rule.setName(getName());
        rule.setFilesizeFilter(getFilersizeFilter());
        rule.setSourceURLFilter(getSourceFilter());
        rule.setFiletypeFilter(getFiletypeFilter());

        rule.setDownloadDestination(cbDest.isSelected() ? fpDest.getPath() : null);
        rule.setChunks(cbChunks.isSelected() ? ((Number) spChunks.getValue()).intValue() : -1);
        rule.setPriority(cbPriority.isSelected() ? prio : null);
        rule.setPackageName(cbPackagename.isSelected() ? txtPackagename.getText() : null);
        rule.setFilename(cbName.isSelected() ? txtNewFilename.getText() : null);
        rule.setAutoExtractionEnabled(cbExtract.isSelected() ? cobExtract.getSelectedIndex() == 0 : null);
        rule.setAutoAddEnabled(cbAdd.isSelected() ? cobAutoAdd.getSelectedIndex() == 0 : null);
        rule.setAutoStartEnabled(cbStart.isSelected() ? cobAutostart.getSelectedIndex() == 0 : null);
        rule.setIconKey(getIconKey());
        rule.setTestUrl(getTxtTestUrl());
        rule.setOnlineStatusFilter(getOnlineStatusFilter());
        rule.setPluginStatusFilter(getPluginStatusFilter());

    }

    private void updateGUI() {

        setIconKey(rule.getIconKey());
        setFilenameFilter(rule.getFilenameFilter());
        setHosterFilter(rule.getHosterURLFilter());
        setName(rule.getName());
        setFilesizeFilter(rule.getFilesizeFilter());
        setSourceFilter(rule.getSourceURLFilter());
        setFiletypeFilter(rule.getFiletypeFilter());
        setOnlineStatusFilter(rule.getOnlineStatusFilter());
        setPluginStatusFilter(rule.getPluginStatusFilter());
        txtPackagename.setText(rule.getPackageName());
        txtNewFilename.setText(rule.getFilename());
        txtTestUrl.setText(rule.getTestUrl());
        fpDest.setPath(rule.getDownloadDestination());
        cbExtract.setSelected(rule.isAutoExtractionEnabled() != null);

        if (rule.getChunks() > 0) {
            spChunks.setValue(rule.getChunks());

        }
        cbStart.setSelected(rule.isAutoStartEnabled() != null);
        cbAdd.setSelected(rule.isAutoAddEnabled() != null);

        cobAutoAdd.setSelectedIndex((rule.isAutoAddEnabled() == null || rule.isAutoAddEnabled()) ? 0 : 1);
        cobAutostart.setSelectedIndex((rule.isAutoStartEnabled() == null || rule.isAutoStartEnabled()) ? 0 : 1);
        cobExtract.setSelectedIndex((rule.isAutoExtractionEnabled() == null || rule.isAutoExtractionEnabled()) ? 0 : 1);
        cbChunks.setSelected(rule.getChunks() > 0);
        cbName.setSelected(!StringUtils.isEmpty(rule.getFilename()));
        cbDest.setSelected(!StringUtils.isEmpty(rule.getDownloadDestination()));
        cbPackagename.setSelected(!StringUtils.isEmpty(rule.getPackageName()));
        cbPriority.setSelected(rule.getPriority() != null);

        prio = rule.getPriority();
        if (prio == null) {
            prio = Priority.DEFAULT;
        }
        switch (prio) {

        case DEFAULT:
            p0.setSelected(true);
            break;
        case HIGH:
            p1.setSelected(true);
            break;
        case HIGHER:
            p2.setSelected(true);
            break;
        case HIGHEST:
            p3.setSelected(true);
            break;
        default:
            p_1.setSelected(true);
            break;

        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private JComboBox createEnabledBox() {
        final JComboBox ret = new JComboBox(new String[] { _GUI._.PackagizerFilterRuleDialog_updateGUI_enabled_(), _GUI._.PackagizerFilterRuleDialog_updateGUI_disabled_() });
        final ListCellRenderer org = ret.getRenderer();
        ret.setRenderer(new ListCellRenderer() {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel r = (JLabel) org.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index < 0) {
                    r.setIcon(ret.getSelectedIndex() == 1 ? NewTheme.I().getIcon("checkbox_false", 14) : NewTheme.I().getIcon("checkbox_true", 14));
                } else {
                    r.setIcon(index == 1 ? NewTheme.I().getIcon("checkbox_false", 14) : NewTheme.I().getIcon("checkbox_true", 14));
                }
                if (!ret.isEnabled()) {
                    r.setIcon(ImageProvider.getDisabledIcon(r.getIcon()));
                }
                return r;
            }
        });
        return ret;
    }

    private PathChooser  fpDest;
    private FilterPanel  fpPriority;
    private ExtTextField txtPackagename;

    private ExtSpinner   spChunks;
    private ExtCheckBox  cbDest;
    private ExtCheckBox  cbPriority;
    private ExtCheckBox  cbPackagename;
    private ExtCheckBox  cbExtract;
    private ExtCheckBox  cbChunks;
    private RadioButton  p_1;
    private RadioButton  p0;
    private RadioButton  p1;
    private RadioButton  p2;
    private RadioButton  p3;
    private JLabel       lblAutostart;
    private JLabel       lblautoadd;
    private ExtCheckBox  cbStart;
    private ExtCheckBox  cbAdd;
    private ExtCheckBox  cbName;
    private JLabel       lblFilename;
    private ExtTextField txtNewFilename;

    @Override
    public JComponent layoutDialogContent() {
        MigPanel ret = (MigPanel) super.layoutDialogContent();
        ret.add(createHeader(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_then()), "gaptop 10, spanx,growx,pushx");
        lblDest = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_dest());
        lblPriority = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_priority());
        lblPackagename = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_packagename());
        lblFilename = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_filename());
        lblExtract = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_extract());
        lblAutostart = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_autostart());
        lblautoadd = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_autoadd());
        lblChunks = createLbl(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_chunks());
        cobExtract = createEnabledBox();
        cobAutostart = createEnabledBox();
        cobAutoAdd = createEnabledBox();
        fpDest = new PathChooser("PackagizerDest") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public JPopupMenu getPopupMenu(ExtTextField txt, CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
                JPopupMenu menu = new JPopupMenu();
                JMenu sub = createVariablesMenu(txt);

                menu.add(sub);
                menu.add(new JSeparator());
                menu.add(cutAction);
                menu.add(copyAction);
                menu.add(pasteAction);
                menu.add(deleteAction);
                menu.add(selectAction);
                return menu;
            }

        };
        fpDest.setHelpText(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_dest_help());
        fpPriority = new FilterPanel("ins 0", "[]0[]8[]0[]8[]0[]8[]0[]8[]0[]", "[]");
        PriorityAction pa_1 = new PriorityAction(Priority.LOWER);
        PriorityAction pa0 = new PriorityAction(Priority.DEFAULT);
        PriorityAction pa1 = new PriorityAction(Priority.HIGH);
        PriorityAction pa2 = new PriorityAction(Priority.HIGHER);
        PriorityAction pa3 = new PriorityAction(Priority.HIGHEST);
        p_1 = new RadioButton(pa_1);

        p0 = new RadioButton(pa0);
        p1 = new RadioButton(pa1);
        p2 = new RadioButton(pa2);
        p3 = new RadioButton(pa3);
        ButtonGroup group = new ButtonGroup();

        group.add(p_1);
        group.add(p0);
        group.add(p1);
        group.add(p2);
        group.add(p3);
        p0.setSelected(true);
        fpPriority.add(getLbl(pa_1));
        fpPriority.add(p_1);
        fpPriority.add(getLbl(pa0));
        fpPriority.add(p0);
        fpPriority.add(getLbl(pa1));
        fpPriority.add(p1);
        fpPriority.add(getLbl(pa2));
        fpPriority.add(p2);
        fpPriority.add(getLbl(pa3));
        fpPriority.add(p3);
        txtPackagename = new ExtTextField() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public JPopupMenu getPopupMenu(CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(createVariablesMenu(txtPackagename));
                menu.add(new JSeparator());
                menu.add(cutAction);
                menu.add(copyAction);
                menu.add(pasteAction);
                menu.add(deleteAction);
                menu.add(selectAction);
                return menu;
            }
        };
        txtPackagename.setHelpText(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_packagename_help_());
        txtNewFilename = new ExtTextField() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public JPopupMenu getPopupMenu(CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(createVariablesMenu(txtNewFilename));
                menu.add(new JSeparator());
                menu.add(cutAction);
                menu.add(copyAction);
                menu.add(pasteAction);
                menu.add(deleteAction);
                menu.add(selectAction);
                return menu;
            }
        };
        txtNewFilename.setHelpText(_GUI._.PackagizerFilterRuleDialog_layoutDialogContent_filename_help_());
        spChunks = new ExtSpinner(new SpinnerNumberModel(2, 1, 20, 1));

        cbDest = new ExtCheckBox(fpDest);
        cbPriority = new ExtCheckBox(fpPriority);
        cbPackagename = new ExtCheckBox(txtPackagename);
        cbExtract = new ExtCheckBox(cobExtract);
        cbStart = new ExtCheckBox(cobAutostart);
        cbAdd = new ExtCheckBox(cobAutoAdd);
        cbChunks = new ExtCheckBox(spChunks);
        cbName = new ExtCheckBox(txtNewFilename);

        ret.add(cbDest);
        ret.add(lblDest, "spanx 2");
        ret.add(fpDest, "spanx,pushx,growx");
        link(cbDest, lblDest, fpDest);

        ret.add(cbPriority);
        ret.add(lblPriority, "spanx 2");
        ret.add(fpPriority, "spanx");
        link(cbPriority, lblPriority, fpPriority);

        ret.add(cbPackagename);
        ret.add(lblPackagename, "spanx 2");
        ret.add(txtPackagename, "spanx,pushx,growx");
        link(cbPackagename, lblPackagename, txtPackagename);

        ret.add(cbName);
        ret.add(lblFilename, "spanx 2");
        ret.add(txtNewFilename, "spanx,pushx,growx");
        link(cbName, lblFilename, txtNewFilename);

        ret.add(cbChunks);
        ret.add(lblChunks, "spanx 2");
        ret.add(spChunks, "spanx,pushx,growx");
        link(cbChunks, lblChunks, spChunks);

        ret.add(cbExtract);
        ret.add(lblExtract, "spanx 2");
        ret.add(cobExtract, "spanx,growx,pushx");

        link(cbExtract, lblExtract, cobExtract);

        ret.add(cbAdd);
        ret.add(lblautoadd, "spanx 2");
        ret.add(cobAutoAdd, "spanx,growx,pushx");

        link(cbAdd, lblautoadd, cobAutoAdd);

        ret.add(cbStart);
        ret.add(lblAutostart, "spanx 2");
        ret.add(cobAutostart, "spanx,growx,pushx");

        link(cbStart, lblAutostart, cobAutostart);

        updateGUI();
        return ret;
    }

    protected JMenu createVariablesMenu(JTextComponent txtPackagename2) {
        JMenu ret = new JMenu(_GUI._.PackagizerFilterRuleDialog_createVariablesMenu_menu());
        // ret.add(new VariableAction(txtPackagename2,
        // _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_hoster(),
        // "<jd:hoster>"));
        // ret.add(new VariableAction(txtPackagename2,
        // _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_source(),
        // "<jd:source>"));
        ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_date(), "<jd:" + PackagizerController.SIMPLEDATE + ":dd.MM.yyyy>"));
        int num = getFilenameFilter().calcPlaceholderCount();
        ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_filename_org(), "<jd:" + PackagizerController.ORGFILENAME + ">"));

        for (int i = 0; i < num; i++) {
            ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_filename((i + 1)), "<jd:" + PackagizerController.ORGFILENAME + ":" + (i + 1) + ">"));
        }
        if (getHosterFilter().isEnabled()) {
            for (int i = 0; i < getHosterFilter().calcPlaceholderCount(); i++) {
                ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_hoster((i + 1)), "<jd:" + PackagizerController.HOSTER + ":" + (i + 1) + ">"));
            }
        }

        if (getSourceFilter().isEnabled()) {
            for (int i = 0; i < getSourceFilter().calcPlaceholderCount(); i++) {
                ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_source((i + 1)), "<jd:" + PackagizerController.SOURCE + ":" + (i + 1) + ">"));
            }
        }
        if (txtPackagename2 != txtPackagename && txtPackagename2 != txtFilename) {
            ret.add(new VariableAction(txtPackagename2, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_packagename(), "<jd:" + PackagizerController.PACKAGENAME + ">"));
        }
        return ret;
    }

    private JLabel getLbl(PriorityAction pa_1) {
        JLabel ret = new JLabel(pa_1.getIcon());
        ret.setToolTipText(pa_1.getTooltipText());
        return ret;
    }

    private void link(final ExtCheckBox cb, JComponent... components) {
        MouseListener ml = new MouseListener() {

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                cb.setSelected(true);
            }
        };
        for (JComponent c : components)
            c.addMouseListener(ml);
    }

    private JLabel createLbl(String packagizerFilterRuleDialog_layoutDialogContent_dest) {
        JLabel ret = new JLabel(packagizerFilterRuleDialog_layoutDialogContent_dest);
        return ret;
    }

}
