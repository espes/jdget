//    jDownloader - Downloadmanager
//    Copyright (C) 2010  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.jdownloader.extensions.folderwatch.columns;

import jd.gui.swing.components.table.JDTableModel;
import jd.gui.swing.components.table.JDTextTableColumn;

import org.jdownloader.extensions.folderwatch.data.HistoryEntry;

public class FilenameColumn extends JDTextTableColumn {

    private static final long serialVersionUID = -2965345959303243243L;

    public FilenameColumn(String name, JDTableModel table) {
        super(name, table);
    }

    @Override
    protected String getStringValue(Object value) {
        return ((HistoryEntry) value).getFilename();
    }

    @Override
    public boolean isEnabled(Object obj) {
        return true;
    }

    @Override
    public boolean isSortable(Object obj) {
        return false;
    }
}
