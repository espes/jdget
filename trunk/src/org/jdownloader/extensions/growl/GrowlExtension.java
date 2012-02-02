//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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

package org.jdownloader.extensions.growl;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jd.Main;
import jd.controlling.JDController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.event.ControlEvent;
import jd.event.ControlListener;
import jd.gui.swing.jdgui.menu.MenuAction;
import jd.nutils.Executer;
import jd.plugins.AddonPanel;
import jd.plugins.DownloadLink;
import jd.utils.JDUtilities;

import org.appwork.controlling.StateEvent;
import org.appwork.controlling.StateEventListener;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.growl.translate.T;

public class GrowlExtension extends AbstractExtension<GrowlConfig> implements ControlListener, StateEventListener {

    private static final String TMP_GROWL_NOTIFICATION_SCPT = "tmp/growlNotification.scpt";

    @Override
    protected void stop() throws StopException {
        JDController.getInstance().removeControlListener(this);
        DownloadWatchDog.getInstance().getStateMachine().removeListener(this);
    }

    @Override
    protected void start() throws StartException {
        File tmp = Application.getResource(TMP_GROWL_NOTIFICATION_SCPT);
        tmp.delete();
        tmp.deleteOnExit();
        try {
            IO.writeToFile(tmp, IO.readURL(getClass().getResource("osxnopasswordforshutdown.scpt")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new StartException(e);
        }

        Main.GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {
                growlNotification(T._.jd_plugins_optional_JDGrowlNotification_started(), getDateAndTime(), "Programstart");
            }

        });
        JDController.getInstance().addControlListener(this);
        DownloadWatchDog.getInstance().getStateMachine().addListener(this);
    }

    @Override
    public String getConfigID() {
        return null;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getDescription() {
        return T._.jd_plugins_optional_jdgrowlnotification_description();
    }

    @Override
    public boolean isLinuxRunnable() {
        return false;
    }

    @Override
    public boolean isWindowsRunnable() {
        return false;
    }

    @Override
    public AddonPanel<GrowlExtension> getGUI() {
        return null;
    }

    @Override
    public ArrayList<MenuAction> getMenuAction() {
        return null;
    }

    public ExtensionConfigPanel<GrowlExtension> getConfigPanel() {
        return null;
    }

    public boolean hasConfigPanel() {
        return false;
    }

    public GrowlExtension() throws StartException {
        super(T._.jd_plugins_optional_jdgrowlnotification());
    }

    public void controlEvent(ControlEvent event) {
        switch (event.getEventID()) {
        case ControlEvent.CONTROL_DOWNLOAD_FINISHED:
            if (event.getCaller() instanceof SingleDownloadController) {
                DownloadLink lastLink = (DownloadLink) event.getParameter();
                growlNotification(T._.jd_plugins_optional_JDGrowlNotification_finished(), lastLink.getName(), "Download complete");
            }
            break;
        }
    }

    private void growlNotification(String headline, String message, String title) {
        if (CrossSystem.isMac()) {
            Executer exec = new Executer("/usr/bin/osascript");
            exec.addParameter(JDUtilities.getResourceFile(GrowlExtension.TMP_GROWL_NOTIFICATION_SCPT).getAbsolutePath());
            exec.addParameter(headline);
            exec.addParameter(message);
            exec.addParameter(title);
            exec.setWaitTimeout(0);
            exec.start();
        }
    }

    private String getDateAndTime() {
        DateFormat dfmt = new SimpleDateFormat("EEEE dd.MM.yy hh:mm:ss");
        return dfmt.format(new Date());
    }

    @Override
    protected void initExtension() throws StartException {
    }

    public void onStateChange(StateEvent event) {
        if (DownloadWatchDog.IDLE_STATE == event.getNewState() || DownloadWatchDog.STOPPED_STATE == event.getNewState()) {
            if (DownloadWatchDog.getInstance().getDownloadssincelastStart() > 0) growlNotification(T._.jd_plugins_optional_JDGrowlNotification_allfinished(), "", "All downloads finished");
        }
    }

    public void onStateUpdate(StateEvent event) {
    }

}