package jd.gui.swing.jdgui.views.settings.panels.linkgrabberfilter.test;

import javax.swing.Icon;

import jd.controlling.linkcrawler.CrawledLink;

import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtFileSizeColumn;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.Files;
import org.jdownloader.DomainInfo;
import org.jdownloader.controlling.filter.LinkgrabberFilterRule;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class SingleFilterResultTableModel extends ExtTableModel<CrawledLink> {

    public SingleFilterResultTableModel() {
        super("SingleFilterResultTableModel");
    }

    @Override
    protected void initColumns() {
        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_filtered_()) {
            @Override
            protected String getTooltipText(final CrawledLink value) {

                try {
                    if (!((LinkgrabberFilterRule) value.getMatchingFilter()).isAccept()) {

                    return _GUI._.ResultTableModel_getTooltipText_dropped_();

                    }
                } catch (Exception e) {

                }
                return _GUI._.ResultTableModel_getTooltipText_accept_();
            }

            public int getDefaultWidth() {

                return 100;
            }

            public boolean isPaintWidthLockIcon() {
                return false;
            }

            protected boolean isDefaultResizable() {

                return false;
            }

            @Override
            protected Icon getIcon(CrawledLink value) {

                try {
                    if (!((LinkgrabberFilterRule) value.getMatchingFilter()).isAccept()) { return NewTheme.I().getIcon("false", 16); }
                } catch (Exception e) {

                }
                return NewTheme.I().getIcon("true", 16);

            }

            @Override
            public String getStringValue(CrawledLink value) {
                try {
                    if (!((LinkgrabberFilterRule) value.getMatchingFilter()).isAccept()) { return _GUI._.ResultTableModel_getStringValue_filtered_(); }
                } catch (Exception e) {

                }
                return _GUI._.ResultTableModel_getStringValue_accepted_();
            }
        });

        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_link_()) {
            {
                editorField.setEditable(false);
            }

            @Override
            public boolean isEditable(final CrawledLink obj) {
                return true;
            }

            public boolean isDefaultVisible() {
                return false;
            }

            @Override
            public String getStringValue(CrawledLink value) {

                return value.getURL();
            }
        });
        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_filename_()) {
            public int getDefaultWidth() {
                return 150;
            }

            protected boolean isDefaultResizable() {

                return true;
            }

            @Override
            public String getStringValue(CrawledLink value) {

                return value.getName();
            }
        });
        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_online_()) {
            @Override
            protected String getTooltipText(final CrawledLink value) {

                switch (value.getLinkState()) {
                case OFFLINE:
                    return _GUI._.ConditionDialog_layoutDialogContent_offline_();
                case ONLINE:
                    return _GUI._.ConditionDialog_layoutDialogContent_online_();
                case TEMP_UNKNOWN:
                    return _GUI._.ConditionDialog_layoutDialogContent_uncheckable_();

                }
                return null;
            }

            public int getDefaultWidth() {

                return 100;
            }

            public boolean isPaintWidthLockIcon() {
                return false;
            }

            protected boolean isDefaultResizable() {

                return false;
            }

            @Override
            protected Icon getIcon(CrawledLink value) {

                switch (value.getLinkState()) {
                case OFFLINE:
                    return NewTheme.getInstance().getIcon("checkbox_false", 16);
                case ONLINE:
                    return NewTheme.getInstance().getIcon("checkbox_true", 16);
                case TEMP_UNKNOWN:
                    return NewTheme.getInstance().getIcon("checkbox_undefined", 16);

                }
                return null;

            }

            @Override
            public String getStringValue(CrawledLink value) {
                switch (value.getLinkState()) {
                case OFFLINE:
                    return _GUI._.ConditionDialog_layoutDialogContent_offline_();
                case ONLINE:
                    return _GUI._.ConditionDialog_layoutDialogContent_online_();
                case TEMP_UNKNOWN:
                    return _GUI._.ConditionDialog_layoutDialogContent_uncheckable_();

                }
                return "";
            }
        });

        addColumn(new ExtFileSizeColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_size_()) {
            public int getDefaultWidth() {
                return 80;
            }

            public boolean isPaintWidthLockIcon() {
                return false;
            }

            protected boolean isDefaultResizable() {

                return false;
            }

            @Override
            protected long getBytes(CrawledLink o2) {
                return o2.getSize();
            }
        });
        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_filetype_()) {
            public int getDefaultWidth() {
                return 65;
            }

            public boolean isPaintWidthLockIcon() {
                return false;
            }

            protected Icon getIcon(final CrawledLink value) {

                return value.getIcon();
            }

            protected boolean isDefaultResizable() {

                return false;
            }

            @Override
            public String getStringValue(CrawledLink value) {

                return Files.getExtension(value.getName());
            }
        });
        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_hoster()) {
            public int getDefaultWidth() {
                return 100;
            }

            public boolean isPaintWidthLockIcon() {
                return false;
            }

            protected boolean isDefaultResizable() {

                return false;
            }

            protected Icon getIcon(final CrawledLink value) {
                return DomainInfo.getInstance(value.getRealHost()).getFavIcon();
            }

            @Override
            public String getStringValue(CrawledLink value) {

                return value.getRealHost();
            }
        });

        addColumn(new ExtTextColumn<CrawledLink>(_GUI._.ResultTableModel_initColumns_source()) {
            {
                editorField.setEditable(false);
            }

            public int getDefaultWidth() {
                return 250;
            }

            public boolean isDefaultVisible() {
                return false;
            }

            @Override
            public boolean isEditable(final CrawledLink obj) {
                return true;
            }

            @Override
            public String getStringValue(CrawledLink value) {
                StringBuilder sb = new StringBuilder();

                CrawledLink p = value;
                String last = p.getURL();
                while ((p = p.getSourceLink()) != null) {
                    if (last != null && last.equals(p.getURL())) continue;
                    sb.append("∟");
                    sb.append(p.getURL());
                    sb.append("\r\n");
                }
                return sb.toString().trim();
            }
        });

    }
}
