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

import java.io.File;
import java.io.IOException;

import jd.PluginWrapper;
import jd.http.RandomUserAgent;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "slingfile.com" }, urls = { "http://(www\\.)?slingfile\\.com/((file|audio|video)/.+|dl/[a-z0-9]+/.*?\\.html)" }, flags = { 0 })
public class SlingFileCom extends PluginForHost {

    public SlingFileCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void correctDownloadLink(DownloadLink link) {
        String theLink = link.getDownloadURL();
        theLink = theLink.replaceAll("(audio|video)", "file");
        link.setUrlDownload(theLink);
    }

    public String getAGBLink() {
        return "http://www.slingfile.com/pages/tos.html";
    }

    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        String waitthat = br.getRegex("Please wait for another (\\d+) minutes to download another file").getMatch(0);
        if (waitthat != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(waitthat) * 60 * 1001l);
        // int wait = 30;
        // String waittime =
        // br.getRegex("\\)\\.innerHTML=\\'(\\d+)\\'").getMatch(0);
        // if (waittime == null) waittime =
        // br.getRegex("id=\"dltimer\">(\\d+)</span><br>").getMatch(0);
        // if (waittime != null) wait = Integer.parseInt(waittime);
        // sleep(wait * 1001l, downloadLink);
        if (br.containsHTML("Please wait until the download is complete")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Too many simultan downloads", 10 * 60 * 1000l);
        /**
         * TODO: Not sure if this will always work, maybe its different if
         * captcha appears after this step
         */
        br.postPage(downloadLink.getDownloadURL(), "download=yes");
        if (br.containsHTML("(api\\.recaptcha\\.net|g>Invalid captcha entered\\. Please try again\\.<)")) {
            PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.parse();
            rc.load();
            File cf = rc.downloadCaptcha(getLocalCaptchaFile());
            String c = getCaptchaCode(cf, downloadLink);
            rc.setCode(c);
            if (br.containsHTML("(api\\.recaptcha\\.net|g>Invalid captcha entered\\. Please try again\\.<)")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        String dllink = br.getRegex("(http://sf[0-9\\-].*?.slingfile\\.com/gdl/[a-z0-9]+/[a-z0-9]+/[a-z0-9]+/[a-z0-9]+/.*?)('|\")").getMatch(0);
        if (dllink == null) dllink = br.getRegex("<td valign=\\'middle\\' align=\\'center\\' colspan=\\'2\\'>[\t\n\r ]+<a href=\"(http://.*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasAutoCaptcha() {
        return true;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(false);
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        br.getPage(downloadLink.getDownloadURL());
        // Prevents errors, i don't know why the page sometimes shows this
        // error!
        if (br.containsHTML(">Please enable cookies to use this website")) br.getPage(downloadLink.getDownloadURL());
        if (br.getRedirectLocation() != null) {
            if (br.getRedirectLocation().equals("http://www.slingfile.com/")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            if (!br.getRedirectLocation().contains("/dl/")) throw new PluginException(LinkStatus.ERROR_FATAL, "Link redirects to a wrong link!");
            downloadLink.setUrlDownload(br.getRedirectLocation());
            logger.info("New link was set in the availableCheck, opening page...");
            br.getPage(downloadLink.getDownloadURL());
        }
        String filename = br.getRegex("<h3>Downloading <span>(.*?)</span></h3>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<title>(.*?) \\- SlingFile \\- Free File Hosting \\& Online Storage</title>").getMatch(0);
        }
        String filesize = br.getRegex("<td>(.{2,20}) \\| Uploaded").getMatch(0);
        if (filesize == null || filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        downloadLink.setName(filename.trim());
        downloadLink.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    public void reset() {
    }

    public void resetDownloadlink(DownloadLink link) {
    }

    public void resetPluginGlobals() {
    }
}