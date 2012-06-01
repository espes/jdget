//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package jd.captcha;

import java.util.logging.Logger;

import jd.captcha.utils.Utilities;
import jd.gui.swing.jdgui.events.EDTEventQueue;
import jd.gui.swing.laf.LookAndFeelController;
import jd.utils.JDUtilities;

/**
 * Jac Training
 * 
 * @author JD-Team
 */
public class JACTrain {

    public static void main(final String args[]) {
        final JACTrain main = new JACTrain();
        main.go();
    }

    private final Logger logger = Utilities.getLogger();

    private void go() {
        final String hoster = "3dl.tv";
        final JAntiCaptcha jac = new JAntiCaptcha(hoster);

        LookAndFeelController.getInstance().setUIManager();
        EDTEventQueue.initEventQueue();
        jac.trainAllCaptchas(JDUtilities.getJDHomeDirectoryFromEnvironment().getAbsolutePath() + "/captchas/3dltv");

        jac.saveMTHFile();
        logger.info("Training Ende");
        // jac.addLetterMap();
        // jac.saveMTHFile();
    }

}