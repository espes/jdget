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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "load.to" }, urls = { "http://(www\\.)?load\\.to/[A-Za-z0-9]+/" }, flags = { 0 })
public class LoadTo extends PluginForHost {

    public LoadTo(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.load.to/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return maxFree.get();
    }

    @Override
    public int getTimegapBetweenConnections() {
        return 2000;
    }

    // note: CAN NOT be negative or zero! (ie. -1 or 0) Otherwise math sections
    // fail. .:. use [1-20]
    private static AtomicInteger           totalMaxSimultanFreeDownload = new AtomicInteger(20);
    // don't touch the following!
    private static AtomicInteger           maxFree                      = new AtomicInteger(1);
    private final String                   INVALIDLINKS                 = "http://(www\\.)?load\\.to/(news|imprint|faq)/";

    private static AtomicReference<String> agent                        = new AtomicReference<String>(null);

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        if (link.getDownloadURL().matches(INVALIDLINKS)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        workAroundTimeOut(br);
        prepareBrowser();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML(">Can\\'t find file")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        final String filename = Encoding.htmlDecode(br.getRegex("<title>([^<>\"]*?) // Load\\.to</title>").getMatch(0));
        final String filesize = br.getRegex("Size: ([^<>\"]*?) <span class=\"space\">").getMatch(0);
        if (filename == null || filesize == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        /* Nochmals das File überprüfen */
        requestFileInformation(downloadLink);
        /* Link holen */
        String linkurl = getLinkurl();
        br.setFollowRedirects(true);
        if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) {
            for (int i = 1; i <= 5; i++) {
                /* Captcha */
                final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
                rc.findID();
                rc.load();
                linkurl = getLinkurl();
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                final String postData = "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + Encoding.urlEncode(c) + "&returnUrl=" + Encoding.urlEncode(br.getURL());
                dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, linkurl, postData, true, 1);
                if (dl.getConnection().getContentType().contains("html")) {
                    br.followConnection();
                    if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) {
                        br.clearCookies("http://load.to/");
                        br.getPage(downloadLink.getDownloadURL());
                        /* Try to avoid block (loop) on captcha reload */
                        br.getHeaders().put("User-Agent", jd.plugins.hoster.MediafireCom.stringUserAgent());
                        continue;
                    }
                }
                break;
            }
            if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) {
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
        } else if (br.containsHTML("solvemedia\\.com/papi/")) {
            for (int i = 1; i <= 3; i++) {
                /* Captcha */
                final PluginForDecrypt solveplug = JDUtilities.getPluginForDecrypt("linkcrypt.ws");
                final jd.plugins.decrypter.LnkCrptWs.SolveMedia sm = ((jd.plugins.decrypter.LnkCrptWs) solveplug).getSolveMedia(br);
                File cf = null;
                try {
                    cf = sm.downloadCaptcha(getLocalCaptchaFile());
                } catch (final Exception e) {
                    if (jd.plugins.decrypter.LnkCrptWs.SolveMedia.FAIL_CAUSE_CKEY_MISSING.equals(e.getMessage())) {
                        throw new PluginException(LinkStatus.ERROR_FATAL, "Host side solvemedia.com captcha error - please contact the " + this.getHost() + " support");
                    }
                    throw e;
                }
                final String code = getCaptchaCode(cf, downloadLink);
                final String chid = sm.getChallenge(code);

                final String postData = "adcopy_response=" + code + "&adcopy_challenge=" + Encoding.urlEncode(chid) + "&returnUrl=" + Encoding.urlEncode(br.getURL());
                dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, linkurl, postData, true, 1);
                if (dl.getConnection().getContentType().contains("html")) {
                    br.followConnection();
                    if (br.containsHTML("solvemedia\\.com/papi/")) {
                        br.clearCookies("http://load.to/");
                        br.getPage(downloadLink.getDownloadURL());
                        /* Try to avoid block (loop) on captcha reload */
                        br.getHeaders().put("User-Agent", jd.plugins.hoster.MediafireCom.stringUserAgent());
                        continue;
                    }
                }
                break;
            }
            if (br.containsHTML("solvemedia\\.com/papi/")) {
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
        } else {
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, linkurl, "", true, 1);
        }
        this.sleep(2 * 1000, downloadLink);
        final URLConnectionAdapter con = dl.getConnection();
        if (con.getResponseCode() == 416) {
            logger.info("Resume failed --> Retrying from zero");
            downloadLink.setChunksProgress(null);
            throw new PluginException(LinkStatus.ERROR_RETRY);
        }
        if (con.getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("file not exist")) {
                logger.info("File maybe offline");
            }
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 30 * 60 * 1000l);
        }
        if (!con.isContentDisposition()) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        try {
            // add a download slot
            controlFree(+1);
            // start the dl
            dl.startDownload();
        } finally {
            // remove download slot
            controlFree(-1);
        }
    }

    private String getLinkurl() throws PluginException {
        String linkurl = Encoding.htmlDecode(new Regex(br, Pattern.compile("\"(http://s\\d+\\.load\\.to/\\?t=\\d+)\"", Pattern.CASE_INSENSITIVE)).getMatch(0));
        if (linkurl == null) {
            linkurl = Encoding.htmlDecode(new Regex(br, Pattern.compile("<form method=\"post\" action=\"(http://.*?)\"", Pattern.CASE_INSENSITIVE)).getMatch(0));
        }
        if (linkurl == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return linkurl;
    }

    private void prepareBrowser() throws IOException {
        if (agent.get() == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            agent.set(jd.plugins.hoster.MediafireCom.stringUserAgent());
        }
        br.getHeaders().put("User-Agent", agent.get());
    }

    /**
     * Prevents more than one free download from starting at a given time. One step prior to dl.startDownload(), it adds a slot to maxFree
     * which allows the next singleton download to start, or at least try.
     * 
     * This is needed because xfileshare(website) only throws errors after a final dllink starts transferring or at a given step within pre
     * download sequence. But this template(XfileSharingProBasic) allows multiple slots(when available) to commence the download sequence,
     * this.setstartintival does not resolve this issue. Which results in x(20) captcha events all at once and only allows one download to
     * start. This prevents wasting peoples time and effort on captcha solving and|or wasting captcha trading credits. Users will experience
     * minimal harm to downloading as slots are freed up soon as current download begins.
     * 
     * @param controlFree
     *            (+1|-1)
     */
    public synchronized void controlFree(final int num) {
        logger.info("maxFree was = " + maxFree.get());
        maxFree.set(Math.min(Math.max(1, maxFree.addAndGet(num)), totalMaxSimultanFreeDownload.get()));
        logger.info("maxFree now = " + maxFree.get());
    }

    @Override
    public void init() {
        Browser.setRequestIntervalLimitGlobal(getHost(), 500);
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean allowHandle(final DownloadLink downloadLink, final PluginForHost plugin) {
        return downloadLink.getHost().equalsIgnoreCase(plugin.getHost());
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
        link.setProperty("error", 0);
    }

    @Override
    public void resetPluginGlobals() {
    }

    /* TODO: remove me after 0.9xx public */
    private void workAroundTimeOut(final Browser br) {
        try {
            if (br != null) {
                br.setConnectTimeout(120 * 1000);
                br.setReadTimeout(120 * 1000);
            }
        } catch (Throwable e) {
        }
    }
}