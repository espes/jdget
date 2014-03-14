package org.jdownloader.gui.views.downloads.columns;

import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import jd.controlling.linkcrawler.ArchiveCrawledPackage;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.gui.swing.jdgui.JDGui;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.swing.components.ExtMergedIcon;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.columnmenu.LockColumnWidthAction;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.StringUtils;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.extraction.ExtractionExtension;
import org.jdownloader.extensions.extraction.bindings.crawledlink.CrawledLinkFactory;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.components.packagetable.LinkTreeUtils;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModel;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModel.TOGGLEMODE;
import org.jdownloader.gui.views.components.packagetable.actions.SortPackagesDownloadOrdnerOnColumn;
import org.jdownloader.gui.views.downloads.action.OpenFileAction;
import org.jdownloader.images.AbstractIcon;
import org.jdownloader.images.NewTheme;
import org.jdownloader.settings.staticreferences.CFG_GUI;

import sun.swing.SwingUtilities2;

public class FileColumn extends ExtTextColumn<AbstractNode> implements GenericConfigEventListener<Boolean> {
    
    /**
     * 
     */
    private static final long serialVersionUID  = -2963955407564917958L;
    protected Border          leftGapBorder;
    private ImageIcon         iconPackageOpen;
    private ImageIcon         iconPackageClosed;
    private ImageIcon         iconArchive;
    private ImageIcon         iconArchiveOpen;
    protected Border          normalBorder;
    private boolean           selectAll         = false;
    private boolean           hideSinglePackage = true;
    
    public FileColumn() {
        super(_GUI._.filecolumn_title());
        leftGapBorder = BorderFactory.createEmptyBorder(0, 32, 0, 0);
        normalBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        iconPackageOpen = NewTheme.I().getIcon("tree_package_open", 32);
        iconArchiveOpen = NewTheme.I().getIcon("tree_archive_open", 32);
        iconArchive = NewTheme.I().getIcon("tree_archive", 32);
        iconPackageClosed = NewTheme.I().getIcon("tree_package_closed", 32);
        setClickcount(0);
        hideSinglePackage = CFG_GUI.HIDE_SINGLE_CHILD_PACKAGES.isEnabled();
        CFG_GUI.HIDE_SINGLE_CHILD_PACKAGES.getEventSender().addListener(this, true);
        this.setRowSorter(new ExtDefaultRowSorter<AbstractNode>() {
            
            public int compare(long x, long y) {
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
            
            @Override
            public int compare(final AbstractNode o1, final AbstractNode o2) {
                String o1s = getStringValue(o1);
                String o2s = getStringValue(o2);
                if (o1s == null) {
                    o1s = "";
                }
                if (o2s == null) {
                    o2s = "";
                }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    int ret = compare(o1s.length(), o2s.length());
                    if (ret == 0) ret = o1s.compareToIgnoreCase(o2s);
                    return ret;
                } else {
                    int ret = compare(o2s.length(), o1s.length());
                    if (ret == 0) ret = o2s.compareToIgnoreCase(o1s);
                    return ret;
                }
            }
            
        });
    }
    
    /**
     * @return
     */
    public JPopupMenu createHeaderPopup() {
        return FileColumn.createColumnPopup(this, (getMinWidth() == getMaxWidth() && getMaxWidth() > 0));
        
    }
    
    public static JPopupMenu createColumnPopup(ExtColumn<AbstractNode> fileColumn, boolean isLocked) {
        final JPopupMenu ret = new JPopupMenu();
        LockColumnWidthAction action;
        boolean sepRequired = false;
        if (!isLocked) {
            sepRequired = true;
            ret.add(new JCheckBoxMenuItem(action = new LockColumnWidthAction(fileColumn)));
        }
        if (fileColumn.isSortable(null)) {
            // if (sepRequired) {
            // ret.add(new JSeparator());
            // }
            sepRequired = true;
            ret.add(new SortPackagesDownloadOrdnerOnColumn(fileColumn));
            // ret.add(new SortPackagesAndLinksDownloadOrdnerOnColumn(this));
        }
        if (sepRequired) {
            ret.add(new JSeparator());
        }
        return ret;
    }
    
    @Override
    public boolean onDoubleClick(MouseEvent e, AbstractNode contextObject) {
        
        if (e.getPoint().x - getBounds().x < 30) { return false; }
        
        if (contextObject instanceof DownloadLink) {
            switch (CFG_GUI.CFG.getLinkDoubleClickAction()) {
                case NOTHING:
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    break;
                case OPEN_FILE:
                    if (CrossSystem.isOpenFileSupported()) {
                        new OpenFileAction(new File(((DownloadLink) contextObject).getFileOutput())).actionPerformed(null);
                    }
                    break;
                case OPEN_FOLDER:
                    if (CrossSystem.isOpenFileSupported()) {
                        new OpenFileAction(LinkTreeUtils.getDownloadDirectory(((DownloadLink) contextObject).getParentNode())).actionPerformed(null);
                    }
                    break;
                case RENAME:
                    startEditing(contextObject);
                    break;
            }
        } else if (contextObject instanceof CrawledLink) {
            switch (CFG_GUI.CFG.getLinkDoubleClickAction()) {
                case NOTHING:
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    break;
                case OPEN_FILE:
                    if (CrossSystem.isOpenFileSupported()) {
                        new OpenFileAction(LinkTreeUtils.getDownloadDirectory(contextObject)).actionPerformed(null);
                    }
                    break;
                case OPEN_FOLDER:
                    if (CrossSystem.isOpenFileSupported()) {
                        new OpenFileAction(LinkTreeUtils.getDownloadDirectory(((CrawledLink) contextObject).getParentNode())).actionPerformed(null);
                    }
                    break;
                case RENAME:
                    startEditing(contextObject);
                    break;
            }
        } else if (contextObject instanceof AbstractPackageNode) {
            switch (CFG_GUI.CFG.getPackageDoubleClickAction()) {
                case EXPAND_COLLAPSE_TOGGLE:
                    if (e.isControlDown() && !e.isShiftDown()) {
                        ((PackageControllerTableModel) getModel()).toggleFilePackageExpand((AbstractPackageNode) contextObject, TOGGLEMODE.BOTTOM);
                    } else if (e.isControlDown() && e.isShiftDown()) {
                        ((PackageControllerTableModel) getModel()).toggleFilePackageExpand((AbstractPackageNode) contextObject, TOGGLEMODE.TOP);
                    } else {
                        ((PackageControllerTableModel) getModel()).toggleFilePackageExpand((AbstractPackageNode) contextObject, TOGGLEMODE.CURRENT);
                    }
                    break;
                case NOTHING:
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    break;
                case OPEN_FOLDER:
                    if (CrossSystem.isOpenFileSupported()) {
                        new OpenFileAction(LinkTreeUtils.getDownloadDirectory(contextObject)).actionPerformed(null);
                    }
                    break;
                case RENAME:
                    startEditing(contextObject);
                    break;
            }
        }
        return true;
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
        
        return true;
    }
    
    public boolean onRenameClick(final MouseEvent e, final AbstractNode obj) {
        if (e.getPoint().x - getBounds().x < 30) { return false; }
        startEditing(obj);
        return true;
        
    }
    
    protected boolean isEditable(final AbstractNode obj, final boolean enabled) {
        return isEditable(obj);
    }
    
    @Override
    protected void setStringValue(final String value, final AbstractNode object) {
        if (StringUtils.isEmpty(value)) return;
        if (object instanceof FilePackage) {
            ((FilePackage) object).setName(value);
        } else if (object instanceof CrawledPackage) {
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
            if (((DownloadLink) value).hasVariantSupport()) {
                if (((DownloadLink) value).getDefaultPlugin().hasVariantToChooseFrom(((DownloadLink) value))) { return new ExtMergedIcon(new AbstractIcon(IconKey.ICON_PACKAGE_OPEN, 16), 0, 3).add(IconIO.getScaledInstance(((DownloadLink) value).getIcon(), 10, 10), 3, -2); }
            }
            return (((DownloadLink) value).getIcon());
        } else if (value instanceof CrawledLink) {
            if (((CrawledLink) value).hasVariantSupport()) {
                if (((CrawledLink) value).gethPlugin().hasVariantToChooseFrom(((CrawledLink) value).getDownloadLink())) { return new ExtMergedIcon(new AbstractIcon(IconKey.ICON_PACKAGE_OPEN, 16), 0, 3).add(IconIO.getScaledInstance(((CrawledLink) value).getIcon(), 10, 10), 3, -2); }
            }
            return (((CrawledLink) value).getIcon());
        }
        return null;
    }
    
    public void configureRendererComponent(AbstractNode value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.rendererIcon.setIcon(this.getIcon(value));
        String str = this.getStringValue(value);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }
        
        if (getTableColumn() != null) {
            this.rendererField.setText(SwingUtilities2.clipStringIfNecessary(rendererField, rendererField.getFontMetrics(rendererField.getFont()), str, getTableColumn().getWidth() - rendererIcon.getPreferredSize().width - 32));
        } else {
            this.rendererField.setText(str);
        }
        if (value instanceof AbstractPackageNode) {
            renderer.setBorder(normalBorder);
        } else if (value instanceof AbstractPackageChildrenNode) {
            AbstractPackageNode parent = ((AbstractPackageNode) ((AbstractPackageChildrenNode) value).getParentNode());
            if (parent != null && parent.getChildren().size() == 1 && hideSinglePackage) {
                renderer.setBorder(normalBorder);
            } else {
                renderer.setBorder(leftGapBorder);
            }
        }
        
    }
    
    @Override
    public void configureEditorComponent(AbstractNode value, boolean isSelected, int row, int column) {
        super.configureEditorComponent(value, isSelected, row, column);
        if (value instanceof AbstractPackageNode) {
            selectAll = true;
            editor.setBorder(normalBorder);
        } else {
            selectAll = false;
            editor.setBorder(leftGapBorder);
        }
        
    }
    
    @Override
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
    }
    
    @Override
    public void focusGained(final FocusEvent e) {
        
        String txt = editorField.getText();
        int point = txt.lastIndexOf(".");
        /* select filename only, try to keep the extension/filetype */
        if (point > 0 && selectAll == false) {
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
        return value.getName();
    }
    
    @Override
    public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
    }
    
    @Override
    public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
        hideSinglePackage = newValue;
    }
    
}
