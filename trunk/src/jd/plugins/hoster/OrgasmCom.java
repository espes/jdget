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
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "orgasm.com" }, urls = { "http://(www\\.)?orgasm\\.com/movies(/recent/[\\w\\+\\%]+)?\\?id=/video/\\d+/" }, flags = { PluginWrapper.DEBUG_ONLY })
public class OrgasmCom extends PluginForHost {

    private String DLLINK = null;

    public OrgasmCom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    public void download(final DownloadLink downloadLink) throws Exception {
        final String playpath = br.getRegex("file: \"(.*?)\",").getMatch(0);
        final String url = br.getRegex("streamer: \"(.*?)\",").getMatch(0);
        if (playpath == null || url == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        DLLINK = url + "@" + playpath;
        dl = new RTMPDownload(this, downloadLink, url + playpath);
        setupRTMPConnection(dl);
        ((RTMPDownload) dl).startDownload();
    }

    @Override
    public String getAGBLink() {
        return "http://www.orgasm.com/termsconditions.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        download(downloadLink);
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("Movie Not Found")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        String filename = br.getRegex("playerHeader\">(.*?)</div>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("title: \"(.*?)\"").getMatch(0);
        }
        if (filename == null) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        downloadLink.setName(Encoding.htmlDecode(filename.trim()) + ".flv");
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }

    private void setupRTMPConnection(final DownloadInterface dl) {
        final jd.network.rtmp.url.RtmpUrlConnection rtmp = ((RTMPDownload) dl).getRtmpConnection();

        rtmp.setResume(false);
        rtmp.setPlayPath(DLLINK.split("@")[1]);
        rtmp.setUrl(DLLINK.split("@")[0]);
        rtmp.setSwfUrl("http://flash.orgasm.com/playerv4.swf");
        /* Im Live-Modus erhält man ein fehlerfreies Videdo */
        rtmp.setLive(true);
    }

}