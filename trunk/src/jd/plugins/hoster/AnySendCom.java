//jDownloader - Downloadmanager
//Copyright (C) 2013  JD-Team support@jdownloader.org
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
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jd.PluginWrapper;
import jd.config.Property;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.JDHash;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "anysend.com" }, urls = { "http://(www\\.)?anysend\\.com/[A-F0-9]{32}" }, flags = { 0 })
public class AnySendCom extends PluginForHost {

    public AnySendCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.anysend.com/terms.html";
    }

    private static StringContainer agent   = new StringContainer();
    private final String           NOSLOTS = ">No slow download slots available";

    public static class StringContainer {
        public String string = null;
    }

    private Browser prepBrowser(Browser prepBr) {
        if (agent.string == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            while (agent.string == null || agent.string.contains("MSIE"))
                agent.string = jd.plugins.hoster.MediafireCom.stringUserAgent();
        }
        prepBr.getHeaders().put("User-Agent", agent.string);
        prepBr.getHeaders().put("Accept-Language", "en-gb, en;q=0.9");
        prepBr.setFollowRedirects(true);
        return prepBr;
    }

    private Browser prepXML(Browser prepBr) {
        prepBr.getHeaders().put("Accept", "*/*");
        prepBr.getHeaders().put("Accept-Charset", null);
        return prepBr;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        prepBrowser(br);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("<title>Removed download \\| AnySend</title>")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        Browser XMLBR = br.cloneBrowser();
        prepXML(XMLBR);
        String visitorid = br.getCookie("http://www.anysend.com/", "PAPVisitorId");
        XMLBR.getPage("http://affiliates.anysend.com/scripts/track.php?accountId=default1&tracking=1&url=H_anysend.com%2F%2F" + getFID(link) + "&referrer=&getParams=&anchor=&isInIframe=false&cookies=");
        visitorid = XMLBR.getRegex("setVisitor\\('([A-Za-z0-9]+)'\\);").getMatch(0);
        String continuelink = br.getRegex("\"(http://(www\\.)?download\\.anysend\\.com/download/download\\.php\\?key=[A-Z0-9]{32}\\&aff=[A-Za-z0-9]+\\&visid=)\"").getMatch(0);
        if (continuelink == null || visitorid == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        br.setCookie("http://www.anysend.com/", "PAPVisitorId", visitorid);
        continuelink += visitorid;
        br.getPage(continuelink);
        if (br.containsHTML(">Your download is no longer available")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("class=\"filename\">([^<>\"]+)</h1>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex(">File Name: (.*?)<").getMatch(0);
            if (filename == null) {
                filename = br.getRegex(">File Name:</div>(.*?)</div>").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("class=\"file\\-name\">([^<>\"]*?)(\\.\\.\\.)?</div>").getMatch(0);
                }
            }
        }
        String filesize = br.getRegex("id=\"files-size\">([^<>\"]+)</td>").getMatch(0);
        if (filesize == null) {
            filesize = br.getRegex(">File Size: (\\d+((\\.|,)\\d+)? ?(KB|MB|GB))<").getMatch(0);
            if (filesize == null) {
                filesize = br.getRegex(">File Size:</div>(.*?)</div>").getMatch(0);
                if (filesize == null) {
                    filesize = br.getRegex("class=\"file\\-size\">([^<>\"]*?)</span>").getMatch(0);
                }
            }
        }
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize.trim().replace(",", ".")));
        if (br.containsHTML(NOSLOTS)) link.getLinkStatus().setStatusText("No free slots available");
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        if (br.containsHTML(NOSLOTS)) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "No free slots available", 5 * 60 * 1000l);
        String dllink = checkDirectLink(downloadLink, "directlink");
        if (dllink == null) {
            final String a = br.getRegex("a:'([A-Za-z0-9]+)'").getMatch(0);
            final String v = br.getRegex("v:'([A-Za-z0-9]+)'").getMatch(0);
            final String key = new Regex(br.getURL(), "\\?key=([A-Z0-9]+)\\&").getMatch(0);
            if (v == null || key == null || a == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            String code = br.getRegex("var dlcode=md5\\('([A-Za-z0-9]+)'").getMatch(0);
            if (code == null) code = jsUnCrush();
            if (code == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            Browser XMLBR = br.cloneBrowser();
            prepXML(XMLBR);
            final long systime = System.currentTimeMillis();
            final String random = (String.valueOf(new Random().nextLong()).replace("-", "") + String.valueOf(new Random().nextLong()).replace("-", "")).substring(5, 26);
            XMLBR.getPage("http://im.anysend.com/check_file.php?key=" + key + "&callback=jQuery" + random + "_" + systime + "&_=" + (systime - 1));
            final String ip = XMLBR.getRegex("\\d+\\(\"([0-9\\.]*?)\"\\);").getMatch(0);
            if (ip == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            XMLBR = br.cloneBrowser();
            prepXML(XMLBR);

            try {
                final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
                rc.findID();
                rc.load();
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                XMLBR.getPage("http://download.anysend.com/download/getcode.php?a=" + a + "&v=" + v + "&key=" + key + "&code=" + JDHash.getMD5(code + rc.getChallenge() + c).toUpperCase() + "&challenge=" + rc.getChallenge() + "&response=" + Encoding.urlEncode(c));
            } catch (Throwable e) {
                XMLBR.getPage("http://download.anysend.com/download/getcode.php?a=" + a + "&v=" + v + "&key=" + key + "&code=" + code);
            }

            if ("true".equalsIgnoreCase(getResult(XMLBR, "isRecaptchaError")) && "Incorrect response".equalsIgnoreCase(getResult(XMLBR, "recaptchaMessage")))
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            else if ("true".equalsIgnoreCase(getResult(XMLBR, "isError")) && "Not authorized".equalsIgnoreCase(getResult(XMLBR, "error"))) throw new PluginException(LinkStatus.ERROR_RETRY);
            final String dlkey = XMLBR.getRegex("\"dlkey\":\"([A-Za-z0-9]+)\"").getMatch(0);
            if (dlkey == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            XMLBR.getPage("http://" + ip + "/anysend/info/" + key + "?callback=jQuery" + System.currentTimeMillis() + "_" + random + "&_=" + System.currentTimeMillis());
            dllink = "http://" + ip + "/anysend/download/" + dlkey + "/" + Encoding.urlEncode(downloadLink.getName());
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            if (br.getHttpConnection().getResponseCode() == 502) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error!", 30 * 60 * 1000l);
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("directlink", dllink);
        dl.startDownload();
    }

    private String getResult(Browser ibr, String key) {
        if (key == null) return null;
        String result = ibr.getRegex("\"" + key + "\":\"([^\"]+)\"").getMatch(0);
        if (result == null) result = ibr.getRegex("\"" + key + "\":(true|false)").getMatch(0);
        return result;
    }

    private String checkDirectLink(final DownloadLink downloadLink, final String property) {
        String dllink = downloadLink.getStringProperty(property);
        if (dllink != null) {
            try {
                final Browser br2 = br.cloneBrowser();
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

    private String getFID(final DownloadLink dl) {
        return new Regex(dl.getDownloadURL(), "([A-Z0-9]{32})$").getMatch(0);
    }

    private String jsUnCrush() {
        /* compressed with JSCrush */
        String code[] = br.getRegex("code:eval\\((.*?)\\)(\\(.*?\\))").getRow(0);
        if (code == null || code.length < 2) return null;
        String result = null;
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            result = (String) engine.eval(code[0]);
            engine.put("arguments", code[1]);
            for (int i = 0; i < 11; i++) {
                result = new Regex(result, "\\(function\\(\\)\\{(.*?)\\}\\(\\)\\)$").getMatch(0);
                if (result.endsWith("return function(){return f.apply(this,arguments)};")) {
                    result = result.replace("return eval", "return ");
                } else {
                    result = result.replace("return eval", "");
                }
                result = result.replaceAll("return function\\(\\)\\{return .\\.apply\\(this,arguments\\)\\};?", "");
                result = result.replace("for(Y in $", "for(Y=0;$");
                result = result.replace("))with(_.split($[Y]))", ")[Y++];)with(_.split($))");
                result = (String) engine.eval(result);
                if (result == null) {
                    result = (String) engine.get("f");
                    engine.put("f", null);
                    if (result == null) result = (String) engine.eval("a" + code[1]);
                }
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        }
        if (result == null || result.trim().length() == 0 || !result.matches("[0-9a-f]+")) return null;
        return result;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}