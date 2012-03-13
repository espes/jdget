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
import java.util.Random;

import jd.PluginWrapper;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "dailymotion.com" }, urls = { "http://(www\\.)?dailymotion\\.com/video/[a-z0-9]+" }, flags = { 2 })
public class DailyMotionCom extends PluginForHost {

    public String               dllink                 = null;
    private static final String MAINPAGE               = "http://www.dailymotion.com/";
    private static final String REGISTEREDONLY1        = "this content as suitable for mature audiences only";
    private static final String REGISTEREDONLY2        = "You must be logged in, over 18 years old, and set your family filter OFF, in order to watch it";
    private static final String REGISTEREDONLYUSERTEXT = "Download only possible for registered users";
    private String[]            subtitles              = null;

    public DailyMotionCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.dailymotion.com/register");
    }

    public void doFree(DownloadLink downloadLink) throws Exception {
        // They do allow resume and unlimited chunks but resuming or using more
        // than 1 chunk causes problems, the file will then b corrupted!
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dl.startDownload()) {
            if (subtitles != null && subtitles.length != 0) {
                String previousFilename = downloadLink.getName();
                downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.dailymotioncom.downloadingsubtitles", "Downloading subtitles..."));
                for (String subtitlelink : subtitles) {
                    final String language = new Regex(subtitlelink, ".*?\\d+:subtitle_(.{1,4}).srt.*?").getMatch(0);
                    if (language != null)
                        downloadLink.setFinalFileName("Subtitle_" + language + ".srt");
                    else
                        downloadLink.setFinalFileName("Subtitle_" + Integer.toString(new Random().nextInt(1000)) + ".srt");
                    dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, subtitlelink, false, 1);
                    if (!dl.getConnection().getContentType().contains("html")) dl.startDownload();
                }
                downloadLink.setName(previousFilename);
            }
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        ai.setUnlimitedTraffic();
        ai.setStatus("Registered (free) User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.dailymotion.com/de/legal/terms";

    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (dllink.contains(REGISTEREDONLY1) || dllink.contains(REGISTEREDONLY2)) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.dailymotioncom.only4registered", REGISTEREDONLYUSERTEXT));
        doFree(downloadLink);
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        login(account);
        requestFileInformation(link);
        doFree(link);
    }

    private void login(Account account) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getHeaders().put("X-Prototype-Version", "1.6.1");
        br.postPage("http://www.dailymotion.com/pageitem/login?urlback=%2Fde&request=%2Flogin", "form_name=dm_pageitem_login&username=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()) + "&rememberme=1&login_submit=Login");
        if (br.getCookie(MAINPAGE, "sid") == null || br.getCookie(MAINPAGE, "dailymotion_auth") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        br.setFollowRedirects(true);
        br.setCookie("http://www.dailymotion.com", "family_filter", "off");
        br.setCookie("http://www.dailymotion.com", "lang", "en_US");
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("(<title>Dailymotion \\– 404 Not Found</title>|url\\(/images/404_background\\.jpg)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String subtitleStuff = br.getRegex("\"sequence\",  \"(.*?)\"").getMatch(0);
        if (subtitleStuff != null) subtitles = new Regex(Encoding.urlDecode(subtitleStuff, false).replace("\\", ""), "\"(http://static\\d+\\.dmcdn\\.net/static/video/\\d+/\\d+/\\d+:subtitle_[a-z]{1,4}\\.srt\\?\\d+)\"").getColumn(0);
        String filename = br.getRegex("name=\"title\" content=\"Dailymotion \\-(.*?)\\- ein Film").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("videos</a><span> > </span><b>(.*?)</b></div>").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("class=\"title\" title=\"(.*?)\"").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("vs_videotitle:\"(.*?)\"").getMatch(0);
                }
            }
        }
        if (filename == null) {
            logger.warning("filename is null...");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dllink = br.getRegex("addVariable\\(\"sequence\",  \"(.*?)\"").getMatch(0);
        if (dllink != null) {
            String allLinks = Encoding.htmlDecode(dllink).replace("\\", "");
            logger.info("alllinkstext: " + allLinks);
            if (allLinks.contains("Dein Land nicht abrufbar")) {
                // Video not available for your country, let's get the
                // downloadUrl from another place
                logger.info("This video is not available for this country, trying to get the url from another place...");
                dllink = br.getRegex("addVariable\\(\"video\", \"(http://.*?)\"\\)").getMatch(0);
                if (dllink == null) dllink = br.getRegex("\"(http://(www\\.)?dailymotion\\.com/cdn/.*?)\"").getMatch(0);
            } else if (allLinks.contains(REGISTEREDONLY1) || allLinks.contains(REGISTEREDONLY2)) {
                downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.dailymotioncom.only4registered", REGISTEREDONLYUSERTEXT));
                downloadLink.setName(filename + ".mp4");
                dllink = REGISTEREDONLY1 + " " + REGISTEREDONLY2;
                return AvailableStatus.TRUE;
            } else {
                // Prefer HD videos
                getQuality("hd720URL", allLinks);
                if (dllink == null) {
                    getQuality("hqURL", allLinks);
                    if (dllink == null) getQuality("sdURL", allLinks);
                }
            }
        }
        if (dllink == null) {
            logger.warning("dllink is null...");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dllink = dllink.replace("\\", "");
        filename = filename.trim();
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ".mp4");
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(dllink);
            if (!con.getContentType().contains("html")) {
                downloadLink.setDownloadSize(con.getLongContentLength());
            } else {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
        return AvailableStatus.TRUE;
    }

    private void getQuality(String quality, String allLinks) {
        dllink = new Regex(allLinks, "\"" + quality + "\":\"(http:[^<>\"\\']+)\"").getMatch(0);
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