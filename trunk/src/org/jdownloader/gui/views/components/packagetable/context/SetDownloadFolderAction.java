package org.jdownloader.gui.views.components.packagetable.context;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import jd.controlling.IOEQ;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;

import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.DownloadFolderChooserDialog;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.LinkTreeUtils;
import org.jdownloader.translate._JDT;

public abstract class SetDownloadFolderAction<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> extends AppAction {

    protected SelectionInfo<PackageType, ChildrenType> si;
    private File                                       path;
    private boolean                                    retOkay = false;

    public SetDownloadFolderAction(SelectionInfo<PackageType, ChildrenType> si) {

        this.si = si;

        setName(_GUI._.SetDownloadFolderAction_SetDownloadFolderAction_());
        path = LinkTreeUtils.getRawDownloadDirectory(si.getContextPackage());
        setIconKey("save");
    }

    @Override
    public boolean isEnabled() {
        return !si.isEmpty();
    }

    /**
     * checks if the given file is valid as a downloadfolder, this means it must be an existing folder or at least its parent folder must
     * exist
     * 
     * @param file
     * @return
     */
    public static boolean isDownloadFolderValid(File file) {
        if (file == null || file.isFile()) return false;
        if (file.isDirectory()) return true;
        File parent = file.getParentFile();
        if (parent != null && parent.isDirectory() && parent.exists()) return true;
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;

        try {

            final File file = dialog(path);
            if (file == null) return;

            retOkay = true;
            IOEQ.add(new Runnable() {

                public void run() {

                    for (PackageType pkg : si.getFullPackages()) {
                        set(pkg, file.getAbsolutePath());

                    }

                    for (final PackageType entry : si.getIncompletePackages()) {

                        try {
                            File oldPath = LinkTreeUtils.getDownloadDirectory(entry);
                            File newPath = file;
                            if (oldPath.equals(newPath)) continue;
                            Dialog.getInstance().showConfirmDialog(Dialog.LOGIC_DONOTSHOW_BASED_ON_TITLE_ONLY | Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN,

                            _JDT._.SetDownloadFolderAction_actionPerformed_(entry.getName()), _JDT._.SetDownloadFolderAction_msg(entry.getName(), si.getSelectedLinksByPackage(entry).size()), null, _JDT._.SetDownloadFolderAction_yes(), _JDT._.SetDownloadFolderAction_no());
                            set(entry, file.getAbsolutePath());

                            continue;
                        } catch (DialogClosedException e) {
                            return;
                        } catch (DialogCanceledException e) {
                            /* user clicked no */
                        }
                        final PackageType pkg = createNewByPrototype(si, entry);
                        set(pkg, file.getAbsolutePath());
                        IOEQ.getQueue().add(new QueueAction<Object, RuntimeException>() {

                            @Override
                            protected Object run() {
                                move(pkg, si.getSelectedLinksByPackage(entry));
                                return null;
                            }

                        });
                    }
                }

            });
        } catch (DialogNoAnswerException e1) {
        }
    }

    protected File dialog(File path) throws DialogClosedException, DialogCanceledException {
        return DownloadFolderChooserDialog.open(path, false, _GUI._.OpenDownloadFolderAction_actionPerformed_object_(si.getContextPackage().getName()));
    }

    abstract protected void move(PackageType pkg, List<ChildrenType> selectedLinksByPackage);

    abstract protected PackageType createNewByPrototype(SelectionInfo<PackageType, ChildrenType> si, PackageType entry);

    protected abstract void set(PackageType pkg, String absolutePath);

}
