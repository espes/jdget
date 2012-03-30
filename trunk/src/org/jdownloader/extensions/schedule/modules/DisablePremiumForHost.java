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

package org.jdownloader.extensions.schedule.modules;

import java.util.List;

import jd.controlling.AccountController;
import jd.plugins.Account;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.jdownloader.extensions.schedule.SchedulerModule;
import org.jdownloader.extensions.schedule.SchedulerModuleInterface;
import org.jdownloader.extensions.schedule.translate.T;

@SchedulerModule
public class DisablePremiumForHost implements SchedulerModuleInterface {

    private static final long serialVersionUID = -8537683716013681486L;

    public boolean checkParameter(String parameter) {
        PluginForHost plg;
        return (plg = JDUtilities.getPluginForHost(parameter)) != null && plg.isPremiumEnabled();
    }

    public void execute(String parameter) {
        List<Account> accs = AccountController.getInstance().list(parameter);
        if (accs != null) {
            for (Account acc : accs) {
                acc.setEnabled(false);
            }
        }
    }

    public String getTranslation() {
        return T._.jd_plugins_optional_schedule_modules_disablePremiumForHost();
    }

    public boolean needParameter() {
        return true;
    }

}