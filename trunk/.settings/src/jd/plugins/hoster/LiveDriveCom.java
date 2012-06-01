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

package jd.plugins.hoster;

import java.io.IOException;

import jd.PluginWrapper;
import jd.http.URLConnectionAdapter;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

//This plugin only takes decrypted links from the livedrive decrypter
@HostPlugin(revision = "$Revision: 15419 $", interfaceVersion = 2, names = { "livedrive.com" }, urls = { "http://[\\w\\.]*?\\.decryptedlivedrive\\.com/frameset\\.php\\?path=/files/\\d+" }, flags = { 0 })
public class LiveDriveCom extends PluginForHost {

    private String DLLINKPART;

    public LiveDriveCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("decryptedlivedrive", "livedrive"));
    }

    @Override
    public String getAGBLink() {
        return "http://www.livedrive.com/terms-of-use";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINKPART + downloadLink.getStringProperty("DOWNLOADID"), true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        String liveDriveUrlUserPart = new Regex(link.getDownloadURL(), "(.*?)\\.livedrive\\.com").getMatch(0);
        liveDriveUrlUserPart = liveDriveUrlUserPart.replaceAll("(http://|www\\.)", "");
        DLLINKPART = "http://" + liveDriveUrlUserPart + ".livedrive.com/WebService/AjaxHandler.ashx?Type=Query&Method=DownloadData&ID=";
        URLConnectionAdapter con = br.openGetConnection(DLLINKPART + link.getStringProperty("DOWNLOADID"));
        if (con.getContentType().contains("html")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else {
            link.setName(getFileNameFromHeader(con));
            link.setDownloadSize(con.getLongContentLength());
            return AvailableStatus.TRUE;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}