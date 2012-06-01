package org.jdownloader.gui.views.downloads.context;

import java.awt.event.ActionEvent;
import java.io.File;

import jd.gui.swing.jdgui.interfaces.ContextMenuAction;

import org.appwork.utils.os.CrossSystem;
import org.jdownloader.gui.translate._GUI;

public class OpenDirectoryAction extends ContextMenuAction {

    private static final long serialVersionUID = 3656369075540437063L;

    private final File        directory;

    public OpenDirectoryAction(File folder) {
        this.directory = folder;
        init();
    }

    @Override
    protected String getIcon() {
        return "package_open";
    }

    @Override
    protected String getName() {
        return _GUI._.gui_table_contextmenu_downloaddir();
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        CrossSystem.openFile(directory);
    }

    @Override
    public boolean isEnabled() {
        return CrossSystem.isOpenFileSupported() && directory != null && directory.exists();
    }

}