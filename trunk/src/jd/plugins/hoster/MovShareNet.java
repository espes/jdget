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
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

//movshare by pspzockerscene
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "movshare.net", "epornik.com" }, urls = { "http://(www\\.)?movshare\\.net/video/[a-z0-9]+", "http://(www\\.)?epornik\\.com/video/[a-z0-9]+" }, flags = { 0, 0 })
public class MovShareNet extends PluginForHost {

    private static final String HUMANTEXT = "We need you to prove you\\'re human";
    private static final String EPRON     = "epornik.com/";

    public MovShareNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.movshare.net/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    // This plugin is 99,99% copy the same as the DivxStageNet plugin, if this
    // gets broken please also check the other one!
    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(true);
        setBrowserExclusive();
        br.getHeaders().put("Accept-Encoding", "");
        br.getPage(downloadLink.getDownloadURL());
        if (!br.getURL().contains(EPRON)) {
            if (br.containsHTML(HUMANTEXT)) {
                Form IAmAHuman = br.getForm(0);
                if (IAmAHuman == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                /*
                 * needed for stable 09581 working, post without data did not set content length to 0
                 */
                IAmAHuman.put("submit", "");
                br.submitForm(IAmAHuman);
            }
        }
        if (br.containsHTML("(The file is beeing transfered to our other servers|This file no longer exists on our servers)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = (br.getRegex("Title: </strong>(.*?)</td>( <td>)?").getMatch(0));
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        filename = filename.trim();
        if (br.getURL().contains("movshare.net/")) {
            if (filename.equals("Untitled") || filename.equals("Title")) {
                downloadLink.setFinalFileName("Video " + new Regex(downloadLink.getDownloadURL(), "movshare\\.net/video/(.+)$").getMatch(0) + ".avi");
            } else {
                downloadLink.setFinalFileName(filename + ".avi");
            }
        } else {
            if (filename.equals("Untitled") || filename.equals("Title")) {
                downloadLink.setFinalFileName("Video " + new Regex(downloadLink.getDownloadURL(), "epornic\\.com/video/(.+)$").getMatch(0) + ".flv");
            } else {
                downloadLink.setFinalFileName(filename + ".flv");
            }
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (!br.getURL().contains(EPRON)) {
            if (br.containsHTML(HUMANTEXT)) {
                Form IAmAHuman = br.getForm(0);
                if (IAmAHuman == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                /*
                 * needed for stable 09581 working, post without data did not set content length to 0
                 */
                br.submitForm(IAmAHuman);
            }
            if (br.containsHTML("The file is beeing transfered to our other servers")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE);
        }
        String dllink = br.getRegex("video/divx\" src=\"(.*?)\"").getMatch(0);
        if (dllink == null) {
            dllink = br.getRegex("src\" value=\"(.*?)\"").getMatch(0);
            if (dllink == null) {
                dllink = br.getRegex("\"file\",\"(http:.*?)\"").getMatch(0);
                if (dllink == null) {
                    dllink = br.getRegex("flashvars\\.file=\"(http:.*?)\"").getMatch(0);
                    if (dllink == null) {
                        final String key = br.getRegex("flashvars\\.filekey=\"(.*?)\"").getMatch(0);
                        if (key != null) {
                            br.getPage("http://www.movshare.net/api/player.api.php?key=" + Encoding.urlEncode(key) + "&user=undefined&codes=undefined&pass=undefined&file=" + new Regex(downloadLink.getDownloadURL(), "movshare\\.net/video/(.+)").getMatch(0));
                            dllink = br.getRegex("url=(http://.*?)\\&title=").getMatch(0);
                        }
                    }
                }
            }
        }
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            if (dl.getConnection().getResponseCode() == 410) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }
}