//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org  http://jdownloader.org
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
//

package jd;

import org.appwork.shutdown.ShutdownController;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.jackson.JacksonMapper;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.utils.os.CrossSystem;

public class Main {

    static {
        org.appwork.utils.Application.setApplication(".jd_home");
        org.appwork.utils.Application.getRoot(jd.Launcher.class);
    }

    public static void checkLanguageSwitch(final String[] args) {
        try {
            String lng = JSonStorage.restoreFrom("cfg/language.json", TranslationFactory.getDesiredLanguage());
            TranslationFactory.setDesiredLanguage(lng);

            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-translatortest")) {
                    TranslationFactory.setDesiredLanguage(args[i + 1]);
                }

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // USe Jacksonmapper in this project
            JSonStorage.setMapper(new JacksonMapper());
            /* FAKE Commit for coalado */
            checkLanguageSwitch(args);
            try {
                /* set D3D Property if not already set by user */
                if (CrossSystem.isWindows() && System.getProperty("sun.java2d.d3d", null) == null) {
                    if (JsonConfig.create(org.jdownloader.settings.GraphicalUserInterfaceSettings.class).isUseD3D()) {
                        System.setProperty("sun.java2d.d3d", "true");
                    } else {
                        System.setProperty("sun.java2d.d3d", "false");
                    }
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            }
            ShutdownController.getInstance().addShutdownEvent(RunUpdaterOnEndAtLeastOnceDaily.getInstance());
            jd.Launcher.mainStart(args);
            if (CrossSystem.isWindows()) {
                /* Main Thread must not die for JavaExe Extensions to work */
                while (true) {
                    Thread.sleep(10000);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                org.appwork.utils.logging.Log.exception(e);
            } catch (Throwable e2) {
            }
            try {
                final org.appwork.utils.swing.dialog.ExceptionDialog dialog = new org.appwork.utils.swing.dialog.ExceptionDialog(org.appwork.utils.swing.dialog.Dialog.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT | org.appwork.utils.swing.dialog.Dialog.BUTTONS_HIDE_CANCEL | org.appwork.utils.swing.dialog.Dialog.STYLE_HIDE_ICON, "Exception occured", "An unexpected error occured.\r\nJDownloader will try to fix this. If this happens again, please contact our support.", e, null, null);

                org.appwork.utils.swing.dialog.Dialog.getInstance().showDialog(dialog);
            } catch (Throwable e2) {
            }
            try {
                org.jdownloader.controlling.JDRestartController.getInstance().restartViaUpdater(false);
            } catch (Throwable e2) {
            }
        }
    }
}