package org.appwork.utils.logging2.sendlogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import org.appwork.swing.action.BasicAction;
import org.appwork.swing.exttable.AlternateHighlighter;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtComponentRowHighlighter;
import org.appwork.swing.exttable.ExtTable;
import org.appwork.utils.ColorUtils;

public class LogTable extends ExtTable<LogFolder> {

    public LogTable(final LogModel model) {
        super(model);
        setShowVerticalLines(true);
        setShowGrid(true);
        setShowHorizontalLines(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  

        final Color b2 = getForeground();
        final Color f2 = getBackground();
        getModel().addExtComponentRowHighlighter(new ExtComponentRowHighlighter<LogFolder>(f2, b2, null) {

            @Override
            public boolean accept(final ExtColumn<LogFolder> column, final LogFolder value, final boolean selected, final boolean focus, final int row) {
                return selected;
            }

        });

        addRowHighlighter(new AlternateHighlighter(null, ColorUtils.getAlphaInstance(new JLabel().getForeground(), 6)));
        setIntercellSpacing(new Dimension(0, 0));
    }

    @Override
    protected JPopupMenu onContextMenu(final JPopupMenu popup, final LogFolder contextObject, final java.util.List<LogFolder> selection, final ExtColumn<LogFolder> column, final MouseEvent mouseEvent) {
        popup.add(new BasicAction() {
            {
                setName(T.T.LogTable_onContextMenu_enable_());
            }

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final LogFolder f : selection) {
                    f.setSelected(true);
                }
                repaint();
            }
        });
        popup.add(new BasicAction() {
            {
                setName(T.T.LogTable_onContextMenu_disable_());
            }

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final LogFolder f : selection) {
                    f.setSelected(false);
                }
                repaint();
            }
        });
        return popup;
    }

}
