//jDownloader - Downloadmanager
//Copyright (C) 2010  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.File;
import java.io.IOException;

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.InputField;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.plugins.decrypter.LnkCrptWs;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "peeje.com" }, urls = { "https?://(www\\.)?peeje(share)?\\.com/files/\\d+/[^<>\"\\'/]+" }, flags = { 0 })
public class PeeJeCom extends PluginForHost {

    // DEV NOTES
    // non account: 20 * unlimited
    // protocol: http + https
    // captchatype: solvemedia
    // other: no redirects

    // avoid dupes across domains/url types.
    @Override
    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll("(www\\.)?peeje\\.com/", "peejeshare.com/").replace(".html", ""));
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasAutoCaptcha() {
        return false;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    public PeeJeCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.peeje.com/terms";
    }

    private static final String PASSWORDTEXT = ">This file is password\\-protected";
    private static final String SLOTSFILLED  = ">All download slots for this file are currently filled";

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        // Offline1
        if (br.containsHTML("Page not found")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        // Offline2
        if (br.containsHTML(">The file you requested does not exist")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final Regex fileInfo = br.getRegex(">File information:<b>([^<>\"]*?)\\- (\\d+(\\.\\d+)? [A-Za-z]{1,5} ?)</b>");
        String filename = null;
        if (br.containsHTML(PASSWORDTEXT) || br.containsHTML(SLOTSFILLED)) {
            filename = br.getRegex("var RELPATH = \"([^<>\"]*?)(\\.html)?\"").getMatch(0);
        } else {
            filename = fileInfo.getMatch(0);
            if (filename == null) filename = br.getRegex("<title>Download ([^<>\"/]+)</title>").getMatch(0);
        }
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        String filesize = fileInfo.getMatch(1);
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String passCode = downloadLink.getStringProperty("pass", null);
        Form dlForm = getFormByKey("download", "Create+Download+Link");
        if (dlForm == null) {
            logger.warning("Could not find download form");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dlForm.setAction(br.getURL());
        if (br.containsHTML(PASSWORDTEXT)) {
            if (passCode == null) passCode = Plugin.getUserInput("Password?", downloadLink);
            if (passCode != null && !passCode.equals("")) dlForm.put("psw", Encoding.urlEncode(passCode));
        }
        if (br.containsHTML("solvemedia\\.com/papi/")) {
            logger.info("Detected captcha method \"solvemedia\" for this host");
            final PluginForDecrypt solveplug = JDUtilities.getPluginForDecrypt("linkcrypt.ws");
            jd.plugins.decrypter.LnkCrptWs.SolveMedia sm = ((LnkCrptWs) solveplug).getSolveMedia(br);
            int repeat = 5;
            // solve media validates captchas response. Incorrect captchas will result in error message within sm.smBr or chid = null.
            for (int i = 0; i <= repeat; i++) {
                if (i == repeat) { throw new PluginException(LinkStatus.ERROR_CAPTCHA); }
                File cf = sm.downloadCaptcha(getLocalCaptchaFile());
                String code = getCaptchaCode(cf, downloadLink);
                String chid = sm.getChallenge(code);
                if (sm.smBr.containsHTML("error-msg\">Try again")) {
                    sm = ((LnkCrptWs) solveplug).getSolveMedia(sm.smBr);
                    continue;
                } else if (chid != null) {
                    dlForm.put("adcopy_challenge", chid);
                    dlForm.put("adcopy_response", Encoding.urlEncode(code));
                    break;
                } else {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
            }
        }
        br.submitForm(dlForm);
        if (br.containsHTML(">Invalid Password, please try again") || br.containsHTML(PASSWORDTEXT)) throw new PluginException(LinkStatus.ERROR_RETRY, "Wrong password entered");
        // Skip ad sh1t
        if (br.getURL().matches(".+(adf\\.ly|adblue\\.us)/.+")) br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML(SLOTSFILLED)) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "No slots available", 10 * 60 * 1000l);
        String dllink = br.getRegex("<a href=\"(https?://[^<>\"]*?)\"><b>Click here to Download</b>").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\"(https?://ww\\d+\\.peeje\\.com/dl/[^<>\"\\']*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.getURL().contains("/upload")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (passCode != null) downloadLink.setProperty("pass", passCode);
        dl.startDownload();
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    // TODO: remove this when v2 becomes stable. use br.getFormbyKey(String key, String value)
    /**
     * Returns the first form that has a 'key' that equals 'value'.
     * 
     * @param key
     * @param value
     * @return
     * */
    private Form getFormByKey(final String key, final String value) {
        Form[] workaround = br.getForms();
        if (workaround != null) {
            for (Form f : workaround) {
                for (InputField field : f.getInputFields()) {
                    if (key != null && key.equals(field.getKey())) {
                        if (value == null && field.getValue() == null) return f;
                        if (value != null && value.equals(field.getValue())) return f;
                    }
                }
            }
        }
        return null;
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        return true;
    }
}