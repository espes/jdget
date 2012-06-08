/*
 * Copyright (C) 2012 Shashank Tulsyan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jdownloader.extensions.neembuu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.neembuu.gui.NeembuuGui;
import org.jdownloader.extensions.neembuu.translate.NeembuuTranslation;
import org.jdownloader.extensions.neembuu.translate._NT;
import org.jdownloader.gui.menu.MenuContext;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEventSender;
import org.jdownloader.gui.menu.eventsender.MenuFactoryListener;
import org.jdownloader.gui.views.downloads.table.DownloadTableContext;
import org.jdownloader.gui.views.linkgrabber.contextmenu.LinkgrabberTableContext;
import org.jdownloader.images.NewTheme;

public class NeembuuExtension extends AbstractExtension<NeembuuConfig, NeembuuTranslation> implements MenuFactoryListener {

    private NeembuuGui                                   tab;
    private int                                          number_of_retries          = 0;                                               // 0=not
    // checked, 1=checked once but failed ... -1 =works :)
    private final LinkedList<WatchAsYouDownloadSession>  watchAsYouDownloadSessions = new LinkedList<WatchAsYouDownloadSession>();
    public static final String                           WATCH_AS_YOU_DOWNLOAD_KEY  = "WATCH_AS_YOU_DOWNLOAD";
    public static final String                           INITIATED_BY_WATCH_ACTION  = "INITIATED_BY_WATCH_ACTION";
    private final Map<FilePackage, NB_VirtualFileSystem> virtualFileSystems         = new HashMap<FilePackage, NB_VirtualFileSystem>();
    private final Map<DownloadLink, DownloadSession>     downloadSessions           = new HashMap<DownloadLink, DownloadSession>();

    public NeembuuExtension() {
        setTitle(_.title());
        System.setProperty("neembuu.vfs.test.MoniorFrame.resumepolicy", "resumeFromPreviousState");
    }

    public String getBasicMountLocation() {
        String basicMntLoc = getSettings().getBasicMountLocation();
        if (basicMntLoc == null) {
            java.io.File basicmntLocfile = new java.io.File(System.getProperty("user.home") + java.io.File.separator + "NeembuuWatchAsYouDownload");
            if (!basicmntLocfile.exists()) {
                basicmntLocfile.mkdir();
            }
            basicMntLoc = basicmntLocfile.getAbsolutePath();
            getSettings().setBasicMountLocation(basicMntLoc);
            return getBasicMountLocation();
        }
        return basicMntLoc;
    }

    public String getVlcLocation() {
        return getSettings().getVLCPath();
    }

    public void setVlcLocation(String vlcl) {
        getSettings().setVLCPath(vlcl);
    }

    public void setBasicMountLocation(String basicMntLoc) throws Exception {
        java.io.File f = new java.io.File(basicMntLoc);
        if (!f.exists()) { throw new IllegalArgumentException("Basic mount location does not exist"); }
        if (!f.isDirectory()) { throw new IllegalArgumentException("Basic mount location must be a directory"); }

        // test basic mount location
        java.io.File f_test = new java.io.File(basicMntLoc, "testfile" + Math.random());
        f_test.createNewFile();
        if (f.exists()) {
            // ok
        }

        boolean del = f_test.delete();
        if (!del) {
            logger.severe("could not delete " + f_test);
            java.nio.file.Files.delete(f_test.toPath());
        }

        getSettings().setBasicMountLocation(basicMntLoc);
    }

    public synchronized final boolean isUsable() {
        if (number_of_retries > 10) {
            logger.fine("Virtual File system checked more than 10 times, and it is not working");
            return false;
            // we just simply assume
            // that it is not going to work
        }
        if (number_of_retries != -1) {
            if (CheckJPfm.checkVirtualFileSystemCompatibility(logger)) {
                number_of_retries = -1;
            } else {
                number_of_retries++;
            }
        }
        return number_of_retries == -1;
    }

    public static boolean canHandle(ArrayList<FilePackage> fps) {
        if (!isActive()) { return false; }
        NeembuuExtension ne = getInstance();
        if (!ne.isUsable()) { return false; }

        for (FilePackage fp : fps) {
            if (fp == null) continue;// ignore empty entries
            for (DownloadLink dl : fp.getChildren()) {
                // todo : make this better. Check other features of host to
                // ensure it can support watch as you download.
                // It would be best if hosts
                // that have been tested successfully/unsuccessfully are added
                // to a whitelist/blacklist.
                if (true) return true;// testing single connection watchAYD
                int c = dl.getDefaultPlugin().getMaxSimultanPremiumDownloadNum();
                if (c < 5 && c != -1) return false;
            }
        }
        return true;
    }

    public static boolean tryHandle(final DownloadSession jdds) {
        if (!isActive()) { return false; }
        NeembuuExtension ne = getInstance();
        if (!ne.isUsable()) { return false; }

        return ne.tryHandle_(jdds);
    }

    private boolean tryHandle_(final DownloadSession jdds) {
        synchronized (watchAsYouDownloadSessions) {

            int o = 0;
            // try{
            // o = Dialog.I().showConfirmDialog(Dialog.LOGIC_COUNTDOWN,
            // _NT._.approve_WatchAsYouDownload_Title(),
            // _NT._.approve_WatchAsYouDownload_Message());
            // }catch(Exception a){
            // ignore
            // }
            // int o =
            // JOptionPane.showConfirmDialog(SwingGui.getInstance().getMainFrame(),
            // _NT._.approve_WatchAsYouDownload_Message(),
            // _NT._.approve_WatchAsYouDownload_Title(),
            // JOptionPane.YES_NO_OPTION);
            // if (o != /*JOptionPane.YES_OPTION*/ Dialog.RETURN_OK) { return
            // false; }

            try {
                WatchAsYouDownloadSessionImpl.makeNew(jdds);
                watchAsYouDownloadSessions.add(jdds.getWatchAsYouDownloadSession());
            } catch (Exception a) {
                /*
                 * SwingUtilities.invokeLater(new Runnable() { // @Override
                 * 
                 * public void run() { JOptionPane.showMessageDialog(SwingGui.getInstance ().getMainFrame(), _NT._.failed_WatchAsYouDownload_Message() + "\n" +
                 * jdds.toString(), _NT._.failed_WatchAsYouDownload_Title(), JOptionPane.ERROR_MESSAGE); } });
                 */

                logger.log(Level.SEVERE, "Could not start a watch as you download session", a);
                return false;
            }
            tab.addSession(jdds);
            downloadSessions.put(jdds.getDownloadLink(), jdds);
            return true;
        }
    }

    public static boolean isActive() {
        return ExtensionController.getInstance().isExtensionActive(NeembuuExtension.class);
    }

    public static NeembuuExtension getInstance() {
        return (NeembuuExtension) ExtensionController.getInstance().getExtension(NeembuuExtension.class)._getExtension();
    }

    public final Map<FilePackage, NB_VirtualFileSystem> getVirtualFileSystems() {
        return virtualFileSystems;
    }

    public final Map<DownloadLink, DownloadSession> getDownloadSessions() {
        return downloadSessions;
    }

    /**
     * Action "onStop". Is called each time the user disables the extension
     */
    @Override
    protected void stop() throws StopException {
        synchronized (watchAsYouDownloadSessions) {
            Iterator<WatchAsYouDownloadSession> it = watchAsYouDownloadSessions.iterator();
            while (it.hasNext()) {
                try {
                    NB_VirtualFileSystem fs = it.next().getVirtualFileSystem();
                    Log.L.info("unmounting " + fs.getMount());
                    fs.unmountAndEndSessions();
                } catch (Exception a) {
                    // ignore
                }
                it.remove();
            }
        }
        MenuFactoryEventSender.getInstance().removeListener(this);
        tab = null;
        Log.L.finer("Stopped " + getClass().getSimpleName());
    }

    /**
     * Actions "onStart". is called each time the user enables the extension
     */
    @Override
    protected void start() throws StartException {
        if (Application.getJavaVersion() < Application.JAVA17) throw new StartException("Java 1.7 needed!");
        Log.L.finer("Started " + getClass().getSimpleName());
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                tab = new NeembuuGui(NeembuuExtension.this);
            }
        }.waitForEDT();

        MenuFactoryEventSender.getInstance().addListener(this, true);
    }

    /**
     * Has to return the Extension MAIN Icon. This icon will be used,for example, in the settings pane
     */
    @Override
    public ImageIcon getIcon(int size) {
        return NewTheme.I().getIcon("ok", size);
    }

    @Override
    public boolean isDefaultEnabled() {
        /* only default enabled if java >= 1.7 installed */
        return Application.getJavaVersion() >= Application.JAVA17;
    }

    @Override
    public boolean isQuickToggleEnabled() {
        return false;
    }

    /**
     * gets called once as soon as the extension is loaded.
     */
    @Override
    protected void initExtension() throws StartException {
    }

    /**
     * Returns the Settingspanel for this extension. If this extension does not have a configpanel, null can be returned
     */
    @Override
    public ExtensionConfigPanel<?> getConfigPanel() {
        return null;
    }

    /**
     * Should return false of this extension has no configpanel
     */
    @Override
    public boolean hasConfigPanel() {
        return false;
    }

    @Override
    public String getAuthor() {
        return "Shashank Tulsyan";
    }

    @Override
    public String getDescription() {
        return _NT._.description();
    }

    /**
     * Returns the gui
     */
    @Override
    public NeembuuGui getGUI() {
        return tab;
    }

    @Override
    public void onExtendPopupMenu(MenuContext<?> context) {

        if (context instanceof LinkgrabberTableContext) {
            onExtendLinkgrabberTablePopupMenu((LinkgrabberTableContext) context);
        } else if (context instanceof DownloadTableContext) {

            onExtendDownloadTablePopupMenu((DownloadTableContext) context);

        }
    }

    private void onExtendLinkgrabberTablePopupMenu(LinkgrabberTableContext context) {
        context.getMenu().add(new WatchAsYouDownloadLinkgrabberAction(context.getSelectionInfo().isShiftDown(), context.getSelectionInfo().getSelectedChildren()));
    }

    private void onExtendDownloadTablePopupMenu(DownloadTableContext context) {
        if (context.getSelectionInfo().isPackageContext()) {
            final HashSet<FilePackage> fps = new HashSet<FilePackage>();
            if (!context.getSelectionInfo().isEmpty()) {
                for (final FilePackage node : context.getSelectionInfo().getAllPackages()) {

                    if ((Boolean) ((FilePackage) node).getProperty(WATCH_AS_YOU_DOWNLOAD_KEY, false) == true) fps.add((FilePackage) node);

                }
            }
            if (fps.size() > 0) context.getMenu().add(new WatchAsYouDownloadAction(new ArrayList<FilePackage>(fps)));
        }

    }
}
