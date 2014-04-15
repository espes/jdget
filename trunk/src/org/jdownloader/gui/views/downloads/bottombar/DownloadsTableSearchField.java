package org.jdownloader.gui.views.downloads.bottombar;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.swing.EDTHelper;
import org.jdownloader.gui.views.components.LinktablesSearchCategory;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTable;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModelFilter;
import org.jdownloader.gui.views.components.packagetable.SearchField;
import org.jdownloader.gui.views.downloads.table.DownloadsTableModel;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;

public final class DownloadsTableSearchField extends SearchField<LinktablesSearchCategory, FilePackage, DownloadLink> {
    protected static DownloadsTableSearchField INSTANCE;

    public DownloadsTableSearchField(PackageControllerTable<FilePackage, DownloadLink> table2Filter, LinktablesSearchCategory defCategory) {
        super(table2Filter, defCategory);
        setCategories(new LinktablesSearchCategory[] { LinktablesSearchCategory.FILENAME, LinktablesSearchCategory.HOSTER, LinktablesSearchCategory.PACKAGE, LinktablesSearchCategory.COMMENT });
        setSelectedCategory(JsonConfig.create(GraphicalUserInterfaceSettings.class).getSelectedDownloadSearchCategory());

        addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setText("");

                }
            }

            public void keyPressed(KeyEvent e) {
            }
        });
    }

    @Override
    public void setSelectedCategory(LinktablesSearchCategory selectedCategory) {
        super.setSelectedCategory(selectedCategory);
        JsonConfig.create(GraphicalUserInterfaceSettings.class).setSelectedDownloadSearchCategory(selectedCategory);
    }

    protected PackageControllerTableModelFilter<FilePackage, DownloadLink> getFilter(final List<Pattern> pattern, LinktablesSearchCategory searchCat) {
        if (searchCat == null || pattern == null || pattern.size() == 0) return null;
        switch (searchCat) {
        case PACKAGE:
            return new PackageControllerTableModelFilter<FilePackage, DownloadLink>() {

                @Override
                public boolean isFilteringPackageNodes() {
                    return true;
                }

                @Override
                public boolean isFilteringChildrenNodes() {
                    return false;
                }

                @Override
                public boolean isFiltered(DownloadLink v) {
                    return false;
                }

                @Override
                public boolean isFiltered(FilePackage e) {
                    for (Pattern filterPattern : pattern) {
                        if (filterPattern.matcher(e.getName()).find()) return false;
                    }
                    return true;
                }

                @Override
                public int getComplexity() {
                    return 0;
                }
            };
        case FILENAME:
            return new PackageControllerTableModelFilter<FilePackage, DownloadLink>() {

                @Override
                public boolean isFilteringPackageNodes() {
                    return false;
                }

                @Override
                public boolean isFilteringChildrenNodes() {
                    return true;
                }

                @Override
                public boolean isFiltered(DownloadLink v) {
                    for (Pattern filterPattern : pattern) {
                        if (filterPattern.matcher(v.getView().getDisplayName()).find()) return false;
                    }
                    return true;
                }

                @Override
                public boolean isFiltered(FilePackage e) {
                    return false;
                }

                @Override
                public int getComplexity() {
                    return 0;
                }
            };
        case COMMENT:
            return new PackageControllerTableModelFilter<FilePackage, DownloadLink>() {

                @Override
                public boolean isFilteringPackageNodes() {
                    return true;
                }

                @Override
                public boolean isFilteringChildrenNodes() {
                    return true;
                }

                @Override
                public boolean isFiltered(DownloadLink v) {
                    for (Pattern filterPattern : pattern) {
                        if (v.getComment() != null && filterPattern.matcher(v.getComment()).find()) return false;
                    }

                    for (Pattern filterPattern : pattern) {
                        if (v.getParentNode().getComment() != null && filterPattern.matcher(v.getParentNode().getComment()).find()) return false;
                    }

                    return true;
                }

                @Override
                public boolean isFiltered(FilePackage fp) {
                    for (Pattern filterPattern : pattern) {
                        if (fp.getComment() != null && filterPattern.matcher(fp.getComment()).find()) return false;
                    }

                    boolean readL = fp.getModifyLock().readLock();

                    try {
                        for (DownloadLink dl : fp.getChildren()) {
                            for (Pattern filterPattern : pattern) {
                                if (dl.getComment() != null && filterPattern.matcher(dl.getComment()).find()) return false;
                            }
                        }
                    } finally {
                        fp.getModifyLock().readUnlock(readL);
                    }

                    return true;
                }

                @Override
                public int getComplexity() {
                    return 1;
                }
            };
        case HOSTER:
            return new PackageControllerTableModelFilter<FilePackage, DownloadLink>() {

                private HashMap<String, Boolean> fastCheck = new HashMap<String, Boolean>();

                @Override
                public boolean isFilteringPackageNodes() {
                    return false;
                }

                @Override
                public boolean isFilteringChildrenNodes() {
                    return true;
                }

                @Override
                public synchronized boolean isFiltered(DownloadLink v) {
                    String host = v.getDomainInfo().getTld();
                    Boolean ret = fastCheck.get(host);
                    if (ret != null) return ret.booleanValue();
                    for (Pattern filterPattern : pattern) {
                        if (filterPattern.matcher(host).find()) {
                            fastCheck.put(host, Boolean.FALSE);
                            return false;
                        }
                    }
                    fastCheck.put(host, Boolean.TRUE);
                    return true;
                }

                @Override
                public boolean isFiltered(FilePackage e) {
                    return false;
                }

                @Override
                public int getComplexity() {
                    return 0;
                }
            };
        }
        return null;
    }

    public static DownloadsTableSearchField getInstance() {
        return new EDTHelper<DownloadsTableSearchField>() {

            @Override
            public DownloadsTableSearchField edtRun() {
                if (INSTANCE != null) return INSTANCE;
                INSTANCE = new DownloadsTableSearchField(DownloadsTableModel.getInstance().getTable(), LinktablesSearchCategory.FILENAME);
                return INSTANCE;
            }

        }.getReturnValue();

    }

}