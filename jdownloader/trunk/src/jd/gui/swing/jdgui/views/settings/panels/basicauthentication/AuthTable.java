package jd.gui.swing.jdgui.views.settings.panels.basicauthentication;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import jd.gui.swing.jdgui.BasicJDTable;

import org.appwork.swing.exttable.ExtColumn;
import org.jdownloader.auth.AuthenticationInfo;

public class AuthTable extends BasicJDTable<AuthenticationInfo> {
    private static final long serialVersionUID = 1L;

    public AuthTable() {
        super(new AuthTableModel());
        this.setSearchEnabled(true);
    }

    @Override
    protected JPopupMenu onContextMenu(JPopupMenu popup, AuthenticationInfo contextObject, java.util.List<AuthenticationInfo> selection, ExtColumn<AuthenticationInfo> col, MouseEvent ev) {
        popup.add(new NewAction(this));
        popup.add(new RemoveAction(this, selection, false));
        return popup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.exttable.ExtTable#onShortcutDelete(java.util.ArrayList
     * , java.awt.event.KeyEvent, boolean)
     */
    @Override
    protected boolean onShortcutDelete(java.util.List<AuthenticationInfo> selectedObjects, KeyEvent evt, boolean direct) {
        new RemoveAction(this, selectedObjects, direct).actionPerformed(null);
        return true;
    }

}
