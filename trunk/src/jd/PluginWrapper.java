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

package jd;

import org.jdownloader.plugins.controller.LazyPlugin;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;

public abstract class PluginWrapper {

    private transient LazyPlugin<?> lazy;

    public PluginWrapper(final LazyPlugin<?> lazy) {
        this.lazy = lazy;
    }

    public LazyPlugin<?> getLazy() {
        return lazy;
    }

    @Deprecated
    public static PluginWrapper getWrapper(String name) {
        LazyHostPlugin ret = HostPluginController.getInstance().get("DirectHTTP");
        if (ret != null) return ret.getPluginWrapper();
        return null;
    }

}
