package org.jdownloader.gui.views.downloads.columns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.FilePackage;

import org.appwork.swing.exttable.columns.ExtDateColumn;
import org.appwork.utils.StringUtils;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.settings.staticreferences.CFG_GUI;

public class AddedDateColumn extends ExtDateColumn<AbstractNode> {

    /**
     * 
     */
    private static final long serialVersionUID = -8841119846403017974L;

    private final String      bad              = _GUI._.added_date_column_invalid();

    public JPopupMenu createHeaderPopup() {

        return FileColumn.createColumnPopup(this, getMinWidth() == getMaxWidth() && getMaxWidth() > 0);

    }

    public AddedDateColumn() {
        super(_GUI._.added_date_column_title());
        rendererField.setHorizontalAlignment(SwingConstants.CENTER);

    }

    @Override
    public boolean isEnabled(AbstractNode obj) {
        if (obj instanceof FilePackage) { return ((FilePackage) obj).getView().isEnabled(); }
        if (obj instanceof CrawledPackage) { return ((CrawledPackage) obj).getView().isEnabled(); }
        return obj.isEnabled();
    }

    @Override
    public boolean isDefaultVisible() {
        return false;
    }

    @Override
    protected boolean isDefaultResizable() {
        return false;
    }

    @Override
    public int getDefaultWidth() {
        return 95;
    }

    @Override
    protected String getBadDateText(AbstractNode value) {
        return bad;
    }

    protected String getDateFormatString() {
        String custom = CFG_GUI.CFG.getDateTimeFormatDownloadListAddedDateColumn();
        if (StringUtils.isNotEmpty(custom)) { return custom; }
        DateFormat sd = SimpleDateFormat.getDateTimeInstance();
        if (sd instanceof SimpleDateFormat) { return ((SimpleDateFormat) sd).toPattern(); }
        return _GUI._.added_date_column_dateformat();
    }

    @Override
    protected Date getDate(AbstractNode node, Date date) {
        if (node.getCreated() <= 0) return null;
        date.setTime(node.getCreated());
        return date;
    }
}
