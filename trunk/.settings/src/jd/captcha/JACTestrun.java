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

import java.io.File;

/**
 * Jac Training
 * 
 * @author JD-Team
 */
public class JACTestrun {

    public static void main(String args[]) {
        JACTestrun main = new JACTestrun();
        main.go();
    }

    private void go() {
        JAntiCaptcha.testMethod(new File("C:\\Users\\coalado\\.jd_home\\jd\\captcha\\methods\\megaupload.com/"));
    }
}