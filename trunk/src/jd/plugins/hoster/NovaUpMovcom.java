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

import jd.PluginWrapper;
import jd.http.URLConnectionAdapter;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "novaup.com" }, urls = { "http://(www\\.)?(nova(up|mov)\\.com/(download|sound|video)/[a-z0-9]+|embed\\.novamov\\.com/embed\\.php\\?width=\\d+\\&height=\\d+\\&v=[a-z0-9]+)" }, flags = { 0 })
public class NovaUpMovcom extends PluginForHost {

    private final String TEMPORARYUNAVAILABLE         = "(The file is being transfered to our other servers\\.|This may take few minutes\\.</)";
    private final String TEMPORARYUNAVAILABLEUSERTEXT = "Temporary unavailable";
    private String       DLLINK                       = "";

    public NovaUpMovcom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        final String videoID = new Regex(link.getDownloadURL(), "embed\\.novamov\\.com/embed\\.php\\?width=\\d+\\&height=\\d+\\&v=([a-z0-9]+)").getMatch(0);
        if (videoID != null) {
            link.setUrlDownload("http://www.novamov.com/video/" + videoID);
        }
    }

    @Override
    public String getAGBLink() {
        return "http://www.novamov.com/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink parameter) throws Exception {
        setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(parameter.getDownloadURL());
        if (br.containsHTML("This file no longer exists on our servers") || br.getURL().contains("novamov.com/index.php")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        // onlinecheck für Videolinks
        if (parameter.getDownloadURL().contains("video")) {
            final String fileId = br.getRegex("flashvars\\.file=\"(.*?)\"").getMatch(0);
            final String fileKey = br.getRegex("flashvars\\.filekey=\"(.*?)\"").getMatch(0);
            final String fileCid = br.getRegex("flashvars\\.cid=\"(.*?)\"").getMatch(0);
            if (fileId == null || fileKey == null || fileCid == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }

            String filename = br.getRegex("name=\"title\" content=\"Watch(.*?)online\"").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("<title>Watch(.*?)online \\| NovaMov - Free and reliable flash video hosting</title>").getMatch(0);
            }
            if (filename == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
            filename = filename.trim() + ".flv";
            parameter.setFinalFileName(filename);
            if (br.containsHTML(TEMPORARYUNAVAILABLE)) {
                parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.novaupmovcom.temporaryunavailable", TEMPORARYUNAVAILABLEUSERTEXT));
                return AvailableStatus.TRUE;
            }
            br.getPage("http://www.novamov.com/api/player.api.php?user=undefined&codes=" + fileCid + "&file=" + fileId + "&pass=undefined&key=" + fileKey);
            DLLINK = br.getRegex("url=(.*?)\\&").getMatch(0);
            if (DLLINK == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
            final URLConnectionAdapter con = br.openGetConnection(DLLINK);
            try {
                parameter.setDownloadSize(con.getLongContentLength());
            } finally {
                con.disconnect();
            }

        } else {
            // Onlinecheck für "nicht"-video Links
            String filename = br.getRegex("<h3><a href=\"#\"><h3>(.*?)</h3></a></h3>").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("style=\"text-indent:0;\"><h3>(.*?)</h3></h5>").getMatch(0);
            }
            final String filesize = br.getRegex("strong>File size :</strong>(.*?)</div>").getMatch(0);
            if (filename == null || filesize == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
            parameter.setName(filename.trim());
            parameter.setDownloadSize(SizeFormatter.getSize(filesize.replaceAll(",", "")));
        }

        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink link) throws Exception {
        if (!link.getDownloadURL().contains("video")) {
            // handling für "nicht"-video Links
            if (br.containsHTML(TEMPORARYUNAVAILABLE)) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, JDL.L("plugins.hoster.novaupmovcom.temporaryunavailable", TEMPORARYUNAVAILABLEUSERTEXT), 30 * 60 * 1000l); }
            br.setFollowRedirects(false);
            final String infolink = link.getDownloadURL();
            br.getPage(infolink);
            DLLINK = br.getRegex("class= \"click_download\"><a href=\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) {
                DLLINK = br.getRegex("\"(http://e\\d+\\.novaup\\.com/dl/[a-z0-9]+/[a-z0-9]+/.*?)\"").getMatch(0);
            }
            if (DLLINK == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
            if (!DLLINK.contains("http://")) {
                DLLINK = "http://novaup.com" + DLLINK;
            }
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}