package org.appwork.utils.logging2.sendlogs;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.appwork.swing.MigPanel;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.swing.dialog.AbstractDialog;

public class SendLogDialog extends AbstractDialog<Object> {

    private java.util.List<LogFolder> folders;
    private LogModel                  model;

    public SendLogDialog(final java.util.List<LogFolder> folders) {
        super(0, T.T.SendLogDialog_SendLogDialog_title_(), null, _AWU.T.lit_continue(), null);
        this.folders = folders;
    }

    @Override
    protected Object createReturnValue() {
        return null;
    }

    @Override
    protected int getPreferredHeight() {

        return 600;
    }

    protected boolean isResizable() {

        return true;
    }

    @Override
    public JComponent layoutDialogContent() {
        final MigPanel p = new MigPanel("ins 0,wrap 1", "[grow,fill]", "[][grow,fill]");
        final JLabel lbl = new JLabel(T.T.SendLogDialog_layoutDialogContent_desc_());
        p.add(lbl);
        model = new LogModel(folders);
        final LogTable table = new LogTable(model);
        p.add(new JScrollPane(table));

        return p;
    }

    public java.util.List<LogFolder> getSelectedFolders() {
        final java.util.List<LogFolder> list = new ArrayList<LogFolder>();
        for (final LogFolder lf : model.getTableData()) {
            if (lf.isSelected()) {
                list.add(lf);

            }
        }
        return list;
    }
}
