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
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.http.Browser;
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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "scribd.com" }, urls = { "http://(www\\.)?scribd\\.com/doc/\\d+" }, flags = { 2 })
public class ScribdCom extends PluginForHost {

    private static final String   formats    = "formats";

    /** The list of server values displayed to the user */
    private static final String[] allFormats;

    private static final String   NODOWNLOAD = JDL.L("plugins.hoster.ScribdCom.NoDownloadAvailable", "Download is disabled for this file!");
    static {
        allFormats = new String[] { "PDF", "TXT" };
    }

    public ScribdCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.scribd.com");
        setConfigElements();
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
        ai.setStatus("Registered (Free) User");
        account.setValid(true);
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://support.scribd.com/forums/33939/entries/25459";
    }

    private String getConfiguredServer() {
        switch (getPluginConfig().getIntegerProperty(formats, -1)) {
        case 0:
            logger.fine("PDF format is configured");
            return "pdf";
        case 1:
            logger.fine("TXT format is configured");
            return "txt";
        default:
            logger.fine("No format is configured, returning PDF...");
            return "pdf";
        }
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
        // Pretend we're using an iphone^^
        br.getHeaders().put("User-Agent", "Apple-iPhone3C1/801.293");
        br.getPage("http://www.scribd.com/mobile/documents/" + new Regex(downloadLink.getDownloadURL(), "scribd\\.com/doc/(\\d+)/").getMatch(0));
        final String dllink = br.getRegex("\"(/mobile/doc/\\d+/download/[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (br.containsHTML(">\\[not available on mobile\\]<")) throw new PluginException(LinkStatus.ERROR_FATAL, NODOWNLOAD);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, "http://www.scribd.com" + dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML(">Can\\'t download that document") || br.getURL().equals(downloadLink.getDownloadURL())) throw new PluginException(LinkStatus.ERROR_FATAL, NODOWNLOAD);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setFinalFileName(downloadLink.getName() + ".pdf");
        dl.startDownload();
    }

    public void handlePremium(DownloadLink parameter, Account account) throws Exception {
        requestFileInformation(parameter);
        if (br.containsHTML("class=\"download_disabled_button\"")) throw new PluginException(LinkStatus.ERROR_FATAL, NODOWNLOAD);
        login(account);
        br.setFollowRedirects(false);
        String fileExt = getConfiguredServer();
        String fileId = new Regex(parameter.getDownloadURL(), "scribd\\.com/doc/(\\d+)/").getMatch(0);
        if (fileId == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        Browser xmlbrowser = br.cloneBrowser();
        xmlbrowser.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        xmlbrowser.postPage("http://www.scribd.com/word/toolbar_download", "id=" + fileId + "&show_container=true&secret_password=nil");
        // Check if the selected format is available
        if (!xmlbrowser.containsHTML("value=\"" + fileExt + "\"")) {
            if (xmlbrowser.containsHTML("premium: true")) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.scribdcom.errors.nofreedownloadlink", "Download is only available for premium users!"));
            throw new PluginException(LinkStatus.ERROR_FATAL, "The selected format is not available for this file!");
        }
        String dllink = "http://www.scribd.com/document_downloads/" + fileId + "?secret_password=&extension=" + fileExt;
        br.getPage(dllink);
        dllink = br.getRedirectLocation();
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, parameter, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        parameter.setFinalFileName(parameter.getName() + "." + fileExt);
        dl.startDownload();
    }

    public void login(Account account) throws Exception {
        setBrowserExclusive();
        br.setFollowRedirects(false);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getPage("http://www.scribd.com");
        br.postPage("http://www.scribd.com/login?from=login_lb", "login_params%5Bcontext%5D=join2&login_or_email=" + Encoding.urlEncode(account.getUser()) + "&login_password=" + Encoding.urlEncode(account.getPass()) + "&commit=Log%20In&form_name=login_lb_form_login_lb");
        if (br.containsHTML("Invalid username or password") || !br.containsHTML("login_successful_lb")) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(false);
        br.getPage(downloadLink.getDownloadURL());
        if (br.getRedirectLocation() != null) {
            if (br.getRedirectLocation().contains("/removal/")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            downloadLink.setUrlDownload(br.getRedirectLocation());
            br.getPage(br.getRedirectLocation());
        }
        String filename = br.getRegex("<meta name=\"title\" content=\"(.*?)\"").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<meta property=\"media:title\" content=\"(.*?)\"").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("<Attribute name=\"title\">(.*?)</Attribute>").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("<h1 class=\"title\" id=\"\">(.*?)</h1>").getMatch(0);
                    if (filename == null) {
                        filename = br.getRegex("<title>(.*?)</title>").getMatch(0);
                    }
                }
            }
        }
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (br.containsHTML("class=\"download_disabled_button\"")) downloadLink.getLinkStatus().setStatusText(NODOWNLOAD);
        downloadLink.setName(filename);
        return AvailableStatus.TRUE;
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

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, getPluginConfig(), formats, allFormats, JDL.L("plugins.host.ScribdCom.formats", "Download files in this format:")).setDefaultValue(0));
    }
}