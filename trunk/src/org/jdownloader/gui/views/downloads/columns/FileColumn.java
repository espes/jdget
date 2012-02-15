package org.jdownloader.gui.views.downloads.columns;

import java.awt.event.FocusEvent;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

import jd.controlling.linkcrawler.ArchiveCrawledPackage;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.gui.swing.jdgui.JDGui;
import jd.plugins.DownloadLink;

import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging.Log;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.extraction.ExtractionExtension;
import org.jdownloader.extensions.extraction.bindings.crawledlink.CrawledLinkFactory;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

import sun.swing.SwingUtilities2;

public class FileColumn extends ExtTextColumn<AbstractNode> {

    /**
     * 
     */
    private static final long serialVersionUID = -2963955407564917958L;
    private Border            leftGapBorder;
    private ImageIcon         iconPackageOpen;
    private ImageIcon         iconPackageClosed;
    private ImageIcon         iconArchive;
    private ImageIcon         iconArchiveOpen;
    private Border            normalBorder;

    public FileColumn() {
        super(_GUI._.filecolumn_title());
        leftGapBorder = BorderFactory.createEmptyBorder(0, 32, 0, 0);
        normalBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        iconPackageOpen = NewTheme.I().getIcon("tree_package_open", 32);
        iconArchiveOpen = NewTheme.I().getIcon("tree_archive_open", 32);
        iconArchive = NewTheme.I().getIcon("tree_archive", 32);
        iconPackageClosed = NewTheme.I().getIcon("tree_package_closed", 32);

    }

    public boolean isPaintWidthLockIcon() {

        return false;
    }

    @Override
    public boolean isEnabled(AbstractNode obj) {
        if (obj instanceof CrawledPackage) { return ((CrawledPackage) obj).getView().isEnabled(); }
        return obj.isEnabled();
    }

    @Override
    public boolean isSortable(AbstractNode obj) {
        return true;
    }

    @Override
    public int getDefaultWidth() {
        return 350;
    }

    @Override
    public boolean isEditable(AbstractNode obj) {
        if (obj instanceof CrawledPackage) return true;
        if (obj instanceof CrawledLink) return true;
        return true;
    }

    protected boolean isEditable(final AbstractNode obj, final boolean enabled) {

        return isEditable(obj);
    }

    @Override
    protected void setStringValue(final String value, final AbstractNode object) {
        if (StringUtils.isEmpty(value)) return;
        if (object instanceof CrawledPackage) {
            ((CrawledPackage) object).setName(value);
        } else if (object instanceof CrawledLink) {
            boolean isMultiArchive = false;

            try {
                ExtractionExtension archiver = ((ExtractionExtension) ExtensionController.getInstance().getExtension(ExtractionExtension.class)._getExtension());
                if (archiver != null) {
                    CrawledLinkFactory clf = new CrawledLinkFactory(((CrawledLink) object));
                    isMultiArchive = archiver.isMultiPartArchive(clf);
                }
            } catch (Throwable e) {
                Log.exception(Level.SEVERE, e);
            }

            ((CrawledLink) object).setName(value);

            if (isMultiArchive) {
                String title = _GUI._.FileColumn_setStringValue_title_();
                String msg = _GUI._.FileColumn_setStringValue_msg_();
                ImageIcon icon = NewTheme.I().getIcon("warning", 32);
                JDGui.help(title, msg, icon);
            }
        }
    }

    @Override
    protected Icon getIcon(AbstractNode value) {

        if (false && value instanceof ArchiveCrawledPackage) {
            return (((AbstractPackageNode<?, ?>) value).isExpanded() ? iconArchiveOpen : iconArchive);
        } else if (value instanceof AbstractPackageNode) {
            return (((AbstractPackageNode<?, ?>) value).isExpanded() ? iconPackageOpen : iconPackageClosed);
        } else if (value instanceof DownloadLink) {
            return (((DownloadLink) value).getIcon());
        } else if (value instanceof CrawledLink) { return (((CrawledLink) value).getIcon()); }
        return null;
    }

    public void configureRendererComponent(AbstractNode value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.configureRendererComponent(value, isSelected, hasFocus, row, column);
        if (value instanceof AbstractPackageNode) {
            renderer.setBorder(normalBorder);
        } else {
            renderer.setBorder(leftGapBorder);
        }

    }

    @Override
    public void configureEditorComponent(AbstractNode value, boolean isSelected, int row, int column) {
        super.configureEditorComponent(value, isSelected, row, column);
        if (value instanceof AbstractPackageNode) {
            editor.setBorder(normalBorder);
        } else {
            editor.setBorder(leftGapBorder);
        }

    }

    @Override
    public void focusGained(final FocusEvent e) {
        String txt = editorField.getText();
        int point = txt.lastIndexOf(".");
        /* select filename only, try to keep the extension/filetype */
        if (point > 0) {
            editorField.select(0, point);
        } else {
            this.editorField.selectAll();
        }

    }

    @Override
    public boolean isHidable() {
        return false;
    }

    @Override
    public final String getStringValue(AbstractNode value) {
        if (value instanceof AbstractPackageNode) {
            return value.getName();

        } else {
            return SwingUtilities2.clipStringIfNecessary(rendererField, rendererField.getFontMetrics(rendererField.getFont()), value.getName(), getTableColumn().getWidth() - rendererIcon.getPreferredSize().width - 5 - 32);

        }

    }

}
