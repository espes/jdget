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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.JSonStorage;
import org.appwork.txtresource.TranslationFactory;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

public class Main {

    private static FileOutputStream outStr;
    private static PrintStream      amprintStream;

    static {
        org.appwork.utils.Application.setApplication(".jd_home");
        org.appwork.utils.Application.getRoot(Launcher.class);

        if (Application.isJared(Main.class)) {
            org.appwork.utils.Application.redirectOutputStreams();
            final Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(new Date().getTime());

            final File file = Application.getResource("JDownloader_" + System.currentTimeMillis() + ".log");

            try {
                file.getParentFile().mkdirs();

                if (!file.isFile()) {
                    file.createNewFile();
                }

                outStr = new FileOutputStream(file, true);

                amprintStream = new PrintStream(outStr);

                System.setErr(amprintStream);
                System.setOut(amprintStream);

            } catch (final IOException e) {
                Log.exception(e);
            }

        }

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

        }
    }

    public static void main(String[] args) {
        try {
            /* FAKE Commit for coalado */
            checkLanguageSwitch(args);

            // workaround.. write all log to a logfile

            ShutdownController.getInstance().addShutdownEvent(RunUpdaterOnEndAtLeastOnceDaily.getInstance());
            jd.Launcher.mainStart(args);
            if (amprintStream != null) {
                ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
                    {
                        setHookPriority(Integer.MIN_VALUE);
                    }

                    @Override
                    public void run() {
                        amprintStream.flush();
                        try {
                            outStr.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
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