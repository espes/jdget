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

package jd.plugins.hoster;

import jd.PluginWrapper;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 15419 $", interfaceVersion = 2, names = { "roms.zophar.net" }, urls = { "http://[\\w\\.]*?roms\\.zophar\\.net/download-file/[0-9]{1,}" }, flags = { 0 })
public class RomsZopharNet extends PluginForHost {

    public RomsZopharNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    // @Override
    public String getAGBLink() {
        return "http://roms.zophar.net/legal.html";
    }

    // @Override
    public int getMaxSimultanFreeDownloadNum() {
        /* TODO: Wert nachprüfen */
        return 1;
    }

    // @Override
    /*
     * public String getVersion() {
     * 
     * return getVersion("$Revision: 15419 $"); }
     */

    // @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(false);
        br.getPage(downloadLink.getDownloadURL());
        jd.plugins.BrowserAdapter.openDownload(br, downloadLink, br.getRedirectLocation()).startDownload();
    }

    // @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) {
        return AvailableStatus.TRUE;
    }

    // @Override
    public void reset() {
    }

    // @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    // @Override
    public void resetPluginGlobals() {
    }
}