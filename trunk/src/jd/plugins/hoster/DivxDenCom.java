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

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.config.Property;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;

import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "divxden.com" }, urls = { "http://(www\\.)?(divxden|vidxden)\\.com/(embed\\-)?[a-z0-9]{12}" }, flags = { 0 })
public class DivxDenCom extends PluginForHost {

    private String              brbefore      = "";
    private static final String COOKIE_HOST   = "http://vidxden.com";
    public boolean              nopremium     = false;
    private static final String INMAINTENANCE = ">This server is in maintenance mode, please try again later.<";

    public DivxDenCom(PluginWrapper wrapper) {
        super(wrapper);
        // this.enablePremium(COOKIE_HOST + "/premium.html");
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(false);
        br.setReadTimeout(3 * 60 * 1000);

        br.setCookie(COOKIE_HOST, "lang", "english");
        br.getPage(link.getDownloadURL());
        doSomething();
        if (brbefore.contains("No such file") || brbefore.contains("No such user exist") || brbefore.contains("File not found") || brbefore.contains(">File Not Found<") || (br.getRedirectLocation() != null && br.getRedirectLocation().contains("/404.html"))) {
            logger.warning("file is 99,99% offline, throwing \"file not found\" now...");
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String filename = br.getRegex("You have requested.*?http://.*?[a-z0-9]{12}/(.*?)</font>").getMatch(0);
        if (filename == null) {
            filename = new Regex(brbefore, "fname\"( type=\"hidden\")? value=\"(.*?)\"").getMatch(1);
            if (filename == null) {
                filename = new Regex(brbefore, "<h2>Download File(.*?)</h2>").getMatch(0);
                if (filename == null) {
                    filename = new Regex(brbefore, "Filename:</b></td><td[ ]{0,2}>(.*?)</td>").getMatch(0);
                    if (filename == null) {
                        filename = new Regex(brbefore, "Filename.*?nowrap.*?>(.*?)</td").getMatch(0);
                        if (filename == null) {
                            filename = new Regex(brbefore, "File Name.*?nowrap>(.*?)</td").getMatch(0);
                        }
                    }
                }
            }
        }
        String filesize = new Regex(brbefore, "\\(([0-9]+ bytes)\\)").getMatch(0);
        if (filesize == null) {
            filesize = new Regex(brbefore, "<small>\\((.*?)\\)</small>").getMatch(0);
            if (filesize == null) {
                filesize = new Regex(brbefore, "</font>[ ]+\\((.*?)\\)(.*?)</font>").getMatch(0);
            }
        }
        if (filename == null || filename.equals("")) {
            if (brbefore.contains("You have reached the download-limit")) {
                logger.warning("Waittime detected, please reconnect to make the linkchecker work!");
                return AvailableStatus.UNCHECKABLE;
            }
            if (brbefore.contains(INMAINTENANCE)) {
                logger.info("Server is in maintenance mode.");
                link.getLinkStatus().setStatusText("Server is in maintenance mode!");
                return AvailableStatus.UNCHECKABLE;
            }
            logger.warning("The filename equals null, throwing \"plugin defect\" now...");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        filename = filename.replaceAll("(</b>|<b>|\\.html)", "");
        link.setFinalFileName(filename.trim());
        if (filesize != null && !filesize.equals("")) {
            logger.info("Filesize found, filesize = " + filesize);
            link.setDownloadSize(SizeFormatter.getSize(filesize));
        }
        return AvailableStatus.TRUE;
    }

    public void checkErrors(DownloadLink theLink, boolean checkAll) throws NumberFormatException, PluginException {
        if (checkAll) {
            if (brbefore.contains("Wrong captcha")) {
                logger.warning("Wrong captcha or wrong password!");
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
        }
        // Some waittimes...
        if (brbefore.contains("You have to wait")) {
            int minutes = 0, seconds = 0, hours = 0;
            String tmphrs = new Regex(brbefore, "You have to wait.*?\\s+(\\d+)\\s+hours?").getMatch(0);
            if (tmphrs != null) hours = Integer.parseInt(tmphrs);
            String tmpmin = new Regex(brbefore, "You have to wait.*?\\s+(\\d+)\\s+minutes?").getMatch(0);
            if (tmpmin != null) minutes = Integer.parseInt(tmpmin);
            String tmpsec = new Regex(brbefore, "You have to wait.*?\\s+(\\d+)\\s+seconds?").getMatch(0);
            if (tmpsec != null) seconds = Integer.parseInt(tmpsec);
            int waittime = ((3600 * hours) + (60 * minutes) + seconds + 1) * 1000;
            if (waittime != 0) {
                logger.info("Detected waittime #1, waiting " + waittime + " milliseconds");
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, waittime);
            } else {
                logger.info("Waittime regexes seem to be broken");
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED);
            }
        }
        if (brbefore.contains("You have reached the download-limit")) {
            String tmphrs = new Regex(brbefore, "\\s+(\\d+)\\s+hours?").getMatch(0);
            String tmpmin = new Regex(brbefore, "\\s+(\\d+)\\s+minutes?").getMatch(0);
            String tmpsec = new Regex(brbefore, "\\s+(\\d+)\\s+seconds?").getMatch(0);
            String tmpdays = new Regex(brbefore, "\\s+(\\d+)\\s+days?").getMatch(0);
            if (tmphrs == null && tmpmin == null && tmpsec == null && tmpdays == null) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, 60 * 60 * 1000l);
            } else {
                int minutes = 0, seconds = 0, hours = 0, days = 0;
                if (tmphrs != null) hours = Integer.parseInt(tmphrs);
                if (tmpmin != null) minutes = Integer.parseInt(tmpmin);
                if (tmpsec != null) seconds = Integer.parseInt(tmpsec);
                if (tmpdays != null) days = Integer.parseInt(tmpdays);
                int waittime = ((days * 24 * 3600) + (3600 * hours) + (60 * minutes) + seconds + 1) * 1000;
                logger.info("Detected waittime #2, waiting " + waittime + "milliseconds");
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, waittime);
            }
        }
        if (brbefore.contains("You're using all download slots for IP")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, 10 * 60 * 1001l);
        if (brbefore.contains("Error happened when generating Download Link")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error!", 10 * 60 * 1000l);
        // Errorhandling for only-premium links
        if (brbefore.contains(" can download files up to ") || brbefore.contains("Upgrade your account to download bigger files") || brbefore.contains("This file reached max downloads") || brbefore.contains(">Upgrade your account to download larger files")) {
            String filesizelimit = new Regex(brbefore, "You can download files up to(.*?)only").getMatch(0);
            if (filesizelimit != null) {
                filesizelimit = filesizelimit.trim();
                logger.warning("As free user you can download files up to " + filesizelimit + " only");
                throw new PluginException(LinkStatus.ERROR_FATAL, "Free users can only download files up to " + filesizelimit);
            } else {
                logger.warning("Only downloadable via premium");
                throw new PluginException(LinkStatus.ERROR_FATAL, "Only downloadable via premium");
            }
        }
    }

    public void checkServerErrors() throws NumberFormatException, PluginException {
        if (brbefore.contains("No file")) throw new PluginException(LinkStatus.ERROR_FATAL, "Server error");
        if (brbefore.contains("File Not Found") || brbefore.contains("<h1>404 Not Found</h1>")) {
            logger.warning("Server says link offline, please recheck that!");
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("divxden.com", "vidxden.com").replace("embed-", ""));
    }

    private String decodeDownloadLink(String s) {
        String decoded = null;

        try {
            Regex params = new Regex(s, "'(.*?[^\\\\])',(\\d+),(\\d+),'(.*?)'");

            String p = params.getMatch(0).replaceAll("\\\\", "");
            int a = Integer.parseInt(params.getMatch(1));
            int c = Integer.parseInt(params.getMatch(2));
            String[] k = params.getMatch(3).split("\\|");

            while (c != 0) {
                c--;
                if (k[c].length() != 0) p = p.replaceAll("\\b" + Integer.toString(c, a) + "\\b", k[c]);
            }

            decoded = p;
        } catch (Exception e) {
        }
        String finallink = null;
        if (decoded != null) {
            finallink = new Regex(decoded, "name=\"src\"value=\"(.*?)\"").getMatch(0);
            if (finallink == null) {
                finallink = new Regex(decoded, "type=\"video/divx\"src=\"(.*?)\"").getMatch(0);
                if (finallink == null) finallink = new Regex(decoded, "s1\\.addVariable\\(\\'file\\',\\'(http://.*?)\\'\\)").getMatch(0);
            }
        }

        return finallink;
    }

    public void doFree(final DownloadLink downloadLink) throws Exception, PluginException {
        String dllink = checkDirectLink(downloadLink, "directlink");
        if (dllink == null) {
            if (brbefore.contains(INMAINTENANCE)) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server is in maintenance mode, try again later.");
            // If the filesize regex above doesn't match you can copy this part into
            // the available status (and delete it here)
            Form[] allForms = br.getForms();
            if (allForms == null || allForms.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            Form freeform = null;
            for (Form singleForm : allForms) {
                if (singleForm.containsHTML("download1")) {
                    freeform = singleForm;
                    break;
                }
            }
            if (freeform != null) {
                freeform.remove("method_premium");
                if (brbefore.contains("solvemedia.com/papi/")) {
                    logger.info("Detected captcha method \"solvemedia\" for this host");
                    final PluginForDecrypt solveplug = JDUtilities.getPluginForDecrypt("linkcrypt.ws");
                    final jd.plugins.decrypter.LnkCrptWs.SolveMedia sm = ((jd.plugins.decrypter.LnkCrptWs) solveplug).getSolveMedia(br);
                    final File cf = sm.downloadCaptcha(getLocalCaptchaFile());
                    final String code = getCaptchaCode(cf, downloadLink);
                    final String chid = sm.getChallenge(code);
                    freeform.put("adcopy_challenge", chid);
                    freeform.put("adcopy_response", "manual_challenge");
                }
                br.submitForm(freeform);
                doSomething();
                if (brbefore.contains("solvemedia.com/papi/")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
            checkErrors(downloadLink, false);
            String md5hash = new Regex(brbefore, "<b>MD5.*?</b>.*?nowrap>(.*?)<").getMatch(0);
            if (md5hash != null) {
                md5hash = md5hash.trim();
                logger.info("Found md5hash: " + md5hash);
                downloadLink.setMD5Hash(md5hash);
            }
            br.setFollowRedirects(false);
            doSomething();
            checkErrors(downloadLink, true);
            dllink = getDllink();
            if (dllink == null) {
                logger.warning("dllink is null...");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        }
        logger.info("Final downloadlink = " + dllink + " starting the download...");
        try {
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        } catch (final ConnectException e) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
        }
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            checkServerErrors();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("directlink", dllink);
        dl.startDownload();
    }

    // Removed fake messages which can kill the plugin
    public void doSomething() throws NumberFormatException, PluginException {
        brbefore = br.toString();
        ArrayList<String> someStuff = new ArrayList<String>();
        ArrayList<String> regexStuff = new ArrayList<String>();
        regexStuff.add("<!(--.*?--)>");
        regexStuff.add("(display: none;\">.*?</div>)");
        regexStuff.add("(visibility:hidden>.*?<)");
        for (String aRegex : regexStuff) {
            String lolz[] = br.getRegex(aRegex).getColumn(0);
            if (lolz != null) {
                for (String dingdang : lolz) {
                    someStuff.add(dingdang);
                }
            }
        }
        for (String fun : someStuff) {
            brbefore = brbefore.replace(fun, "");
        }
    }

    @Override
    public String getAGBLink() {
        return COOKIE_HOST + "/tos.html";
    }

    public String getDllink() {
        String dllink = br.getRedirectLocation();
        if (dllink == null) {
            dllink = new Regex(brbefore, "dotted #bbb;padding.*?<a href=\"(.*?)\"").getMatch(0);
            if (dllink == null) {
                dllink = new Regex(brbefore, "This (direct link|download link) will be available for your IP.*?href=\"(http.*?)\"").getMatch(1);
                if (dllink == null) {
                    dllink = new Regex(brbefore, "Download: <a href=\"(.*?)\"").getMatch(0);
                    if (dllink == null) {
                        String crypted = br.getRegex("p}\\((.*?)\\.split\\('\\|'\\)").getMatch(0);
                        if (crypted != null) dllink = decodeDownloadLink(crypted);
                    }
                }
            }
        }
        return dllink;
    }

    private String checkDirectLink(final DownloadLink downloadLink, final String property) {
        String dllink = downloadLink.getStringProperty(property);
        if (dllink != null) {
            try {
                Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(dllink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    downloadLink.setProperty(property, Property.NULL);
                    dllink = null;
                }
                con.disconnect();
            } catch (Exception e) {
                downloadLink.setProperty(property, Property.NULL);
                dllink = null;
            }
        }
        return dllink;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    public String handlePassword(String passCode, Form pwform, DownloadLink thelink) throws IOException, PluginException {
        if (thelink.getStringProperty("pass", null) == null) {
            passCode = getUserInput(null, thelink);
        } else {
            /* gespeicherten PassCode holen */
            passCode = thelink.getStringProperty("pass", null);
        }
        pwform.put("password", passCode);
        logger.info("Put password \"" + passCode + "\" entered by user in the DLForm.");
        return passCode;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        if (acc == null) {
            /* no account, yes we can expect captcha */
            return true;
        }
        if (Boolean.TRUE.equals(acc.getBooleanProperty("free"))) {
            /* free accounts also have captchas */
            return true;
        }
        return false;
    }
}