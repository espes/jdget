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
import jd.http.Browser;
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

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "x7.to" }, urls = { "http://[\\w\\.]*?x7\\.to/(?!list)[a-zA-Z0-9]+(/(?!inList)[^/\r\n]+)?" }, flags = { 2 })
public class XSevenTo extends PluginForHost {

    public static final Object  LOCK            = new Object();

    private static final String PREMIUMONLYTEXT = "(only premium members will be able to download the file|The requested file is larger than|und kann nur von Premium-Benutzern herunter geladen werden)";

    public XSevenTo(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://x7.to/foyer");
    }

    // @Override
    // public boolean checkLinks(DownloadLink[] urls) {
    // if (urls == null || urls.length == 0) { return false; }
    // try {
    // Browser br = new Browser();
    // br.setDebug(true);
    // br.setCookiesExclusive(true);
    // StringBuilder sb = new StringBuilder();
    // ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
    // int index = 0;
    // while (true) {
    // links.clear();
    // while (true) {
    // /* we test 80 links at once */
    // if (index == urls.length || links.size() > 80) break;
    // DownloadLink dl = urls[index];
    // index++;
    // String id = new Regex(dl.getDownloadURL(),
    // "to/(?!list)([a-zA-Z0-9]+/[^/]+)").getMatch(0);
    // /*
    // * we need id/filename format for api else fallback to
    // * website checking
    // */
    // /* � cause issues for api, so we do fallback check here */
    // if (id == null || id.contains("�") || id.contains("%")) {
    // dl.isAvailable();
    // continue;
    // }
    // links.add(dl);
    // }
    // if (links.size() == 0) break;
    // sb.delete(0, sb.capacity());
    // int c = 0;
    // for (DownloadLink dl : links) {
    // if (c > 0) sb.append("&");
    // String id = new Regex(dl.getDownloadURL(),
    // "to/(?!list)([a-zA-Z0-9]+/[^/]+)").getMatch(0);
    // sb.append("id_" + c + "=" + Encoding.urlEncode(id));
    // c++;
    // }
    // br.postPage("http://x7.to/api?fnc=onlinecheck", sb.toString());
    // String infos[][] =
    // br.getRegex(Pattern.compile("id_(\\d+): ((.+), (\\d+)|file removed)")).getMatches();
    // for (String info[] : infos) {
    // int number = Integer.parseInt(info[0]);
    // DownloadLink dl = links.get(number);
    // if ("file removed".equalsIgnoreCase(info[1])) {
    // dl.setAvailable(false);
    // } else {
    // dl.setAvailable(true);
    // dl.setName(info[2]);
    // dl.setDownloadSize(SizeFormatter.getSize(info[3]));
    // }
    // }
    // if (index == urls.length) break;
    // }
    // } catch (Throwable e) {
    // return false;
    // }
    // return true;
    // }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("/player", ""));
    }

    public void doFree(DownloadLink downloadLink) throws Exception {
        URLConnectionAdapter con = br.openGetConnection(downloadLink.getDownloadURL());
        if (con.getContentType().contains("html")) {
            br.followConnection();
            String dllink = null;
            if (br.containsHTML(PREMIUMONLYTEXT)) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.XSevenTo.errors.only4premium", "Only downloadable for premium users"));
            String fileID = new Regex(downloadLink.getDownloadURL(), "\\.to/([a-zA-Z0-9]+)").getMatch(0);
            boolean isStream = br.containsHTML("<b>Stream</b>");
            if (!isStream) {
                Browser brc = requestXML(br, "http://x7.to/james/ticket/dl/" + fileID, null, false);
                /* error handling */
                if (brc.containsHTML("err:")) {
                    String error = brc.getRegex("err:\"(.*?)\"").getMatch(0);
                    if (error == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    if (error.contains("limit-parallel")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 5 * 60 * 1000l);
                    if (error.contains("limit-dl")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 30 * 60 * 1000l);
                    if (error.contains("Download denied")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerProblem", 30 * 60 * 1000l);
                    /* unknown error */
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                if (brc.containsHTML("type:'download")) {
                    int waitsecs = 0;
                    String waittime = brc.getRegex("wait:(\\d+)").getMatch(0);
                    if (waittime != null) waitsecs = Integer.parseInt(waittime);
                    if (waitsecs > 0) sleep(waitsecs * 1000l, downloadLink);
                    dllink = brc.getRegex("url:'(.*?)'").getMatch(0);
                }
            } else {
                /* free users can only download the 10mins sample */
                br.getPage("http://x7.to/stream/" + fileID + "/h");
                dllink = br.getRedirectLocation();
            }
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            if (!dllink.contains("x7.to/")) dllink = "http://x7.to/" + dllink;
            br.setDebug(true);
            /* streams are not resumable */
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, !isStream, 1);
        } else {
            // Direct links have no limits
            con.disconnect();
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadLink.getDownloadURL(), true, 0);
        }
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("wird ein erneuter Versuch gestartet") || br.containsHTML("elem/tecissue")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Serverproblems");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
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
        account.setValid(true);
        if (account.getStringProperty("freeaccount", null) == null) {
            String validUntil = br.getRegex("Premium member until (.*?)\"").getMatch(0);
            if (validUntil != null) {
                ai.setValidUntil(TimeFormatter.getMilliSeconds(validUntil.trim(), "yyyy-MM-dd HH:mm:ss", null));
            } else {
                if (!br.containsHTML("img/sym/crown.png")) {
                    account.setValid(false);
                    ai.setExpired(true);
                } else {
                    ai.setValidUntil(-1);
                }
            }
            ai.setStatus("Premium User");
            try {
                account.setMaxSimultanDownloads(-1);
            } catch (Throwable e) {

            }
        } else {
            try {
                account.setMaxSimultanDownloads(1);
            } catch (Throwable e) {

            }
            ai.setStatus("Registered (free) User");
        }
        String points = br.getRegex("upoints\".*?>([0-9\\.]*?)<").getMatch(0);
        if (points != null) {
            long p = Long.parseLong(points.trim().replaceFirst("\\.", ""));
            ai.setPremiumPoints(p);
        }
        String money = br.getRegex("id=\"balance\">([0-9\\.,]+)").getMatch(0);
        if (money != null) {
            money = money.replaceFirst(",", ".");
            ai.setAccountBalance(money);
        }
        String remaining = br.getRegex("class=\"aT aR\">.*?\">(.*?)<").getMatch(0);
        String trafficNow = br.getRegex("buyTraffic.*?TrafficNow.*?class=\"aT aR\">.*?\">(.*?)<").getMatch(0);
        if (remaining != null && (remaining.contains("unlimited") || remaining.equals(""))) {
            ai.setSpecialTraffic(true);
        } else {
            long t1 = remaining != null ? SizeFormatter.getSize(remaining) : 0;
            long t2 = trafficNow != null ? SizeFormatter.getSize(trafficNow) : 0;
            ai.setTrafficLeft(t1 + t2);
        }
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://x7.to/legal";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        requestFileInformation(downloadLink);
        login(account);
        if (account.getStringProperty("freeaccount", null) == null) {
            br.setFollowRedirects(false);
            String dllink = null;
            br.getPage(downloadLink.getDownloadURL());
            if (br.getRedirectLocation() != null) {
                dllink = br.getRedirectLocation();
            } else {
                if (br.containsHTML("img/elem/trafficexh_")) throw new PluginException(LinkStatus.ERROR_PREMIUM, JDL.L("plugins.hoster.uploadedto.errorso.premiumtrafficreached", "Traffic limit reached"), PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                dllink = br.getRegex("<b>Download</b>.*?href=\"(http://stor.*?)\"").getMatch(0);
                if (dllink == null && br.containsHTML("<b>Stream</b>")) {
                    /* its a streamdownload */
                    dllink = br.getRegex("window\\.location\\.href='(http://stor.*?)'").getMatch(0);
                }

            }
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                br.followConnection();
                if (br.containsHTML("img/elem/trafficexh_")) throw new PluginException(LinkStatus.ERROR_PREMIUM, JDL.L("plugins.hoster.uploadedto.errorso.premiumtrafficreached", "Traffic limit reached"), PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                if (br.containsHTML("wird ein erneuter Versuch gestartet") || br.containsHTML("elem/tecissue")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Serverproblems");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl.startDownload();
        } else {
            doFree(downloadLink);
        }
    }

    public void login(Account account) throws Exception {
        synchronized (LOCK) {
            this.setBrowserExclusive();
            br.getPage("http://x7.to");
            br.postPage("http://x7.to/james/login", "id=" + Encoding.urlEncode(account.getUser()) + "&pw=" + Encoding.urlEncode(account.getPass()));
            if (br.getCookie("http://x7.to/", "login") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            br.getPage("http://x7.to/my");
            String premium = br.getRegex("Account type<.*?(>Premium<)/").getMatch(0);
            if (premium == null) {
                account.setProperty("freeaccount", "yesss ;)");
            } else {
                account.setProperty("freeaccount", null);
            }
            /* have to call this in order so set language */
            br.getPage("http://x7.to/lang/en");
            br.getPage("http://x7.to/my");
        }
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        if (true) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        this.setBrowserExclusive();
        /* have to call this in order so set language */
        br.getPage("http://x7.to/lang/en");
        br.setFollowRedirects(true);
        // Check if the link is a normal- or a direct link
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(downloadLink.getDownloadURL());
            if (con.getContentType().contains("html")) {
                br.followConnection();
                String filename = br.getRegex("<title>x7\\.to » Download: (.*?)</title>").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("<b>(Download|Stream)</b>.*?<span.*?>(.*?)<").getMatch(1);
                    if (filename != null) {
                        String extension = br.getRegex("<b>(Download|Stream)</b>.*?<span.*?>.*?<small.*?>(.*?)<").getMatch(1);
                        if (extension == null) extension = "";
                        filename = filename.trim() + extension;
                    }
                }
                String filesize = br.getRegex("<b>(Download|Stream)</b>.*?\\((.*?)\\)").getMatch(1);
                if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                downloadLink.setName(Encoding.htmlDecode(filename.trim()));
                if (filesize.contains(",") && filesize.contains(".")) {
                    /* workaround for 1.000,00 MB bug */
                    filesize = filesize.replaceFirst("\\.", "");
                }
                downloadLink.setDownloadSize(SizeFormatter.getSize(filesize.replaceAll(",", ".")));
                if (br.containsHTML(PREMIUMONLYTEXT)) downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.XSevenTo.errors.only4premium", "Only downloadable for premium users"));
            } else {
                downloadLink.getLinkStatus().setStatusText("Direct link");
                downloadLink.setName(getFileNameFromHeader(con));
                downloadLink.setDownloadSize(con.getLongContentLength());
            }
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
        return AvailableStatus.TRUE;
    }

    private Browser requestXML(Browser br, String url, String post, boolean clonebrowser) throws IOException {
        Browser brc = br;
        if (clonebrowser) brc = br.cloneBrowser();
        brc.setDebug(true);
        brc.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        brc.postPage(url, post != null ? post : "");
        brc.getHeaders().remove("X-Requested-With");
        return brc;
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