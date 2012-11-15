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
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jd.PluginWrapper;
import jd.http.RandomUserAgent;
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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "zippyshare.com" }, urls = { "http://www\\d{0,}\\.zippyshare\\.com/(v/\\d+/file\\.html|.*?key=\\d+)" }, flags = { 0 })
public class Zippysharecom extends PluginForHost {

    private String DLLINK = null;

    public Zippysharecom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        prepareBrowser(downloadLink);
        if (br.containsHTML("(File has expired and does not exist anymore on this server|<title>Zippyshare.com \\- File does not exist</title>|File does not exist on this server)")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }

        String filename = br.getRegex("<title>Zippyshare\\.com \\- (.*?)</title>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex(Pattern.compile("Name:(\\s+)?</font>(\\s+)?<font style=.*?>(.*?)</font>", Pattern.CASE_INSENSITIVE)).getMatch(2);
        }
        if (filename != null && filename.contains("fileName?key=")) filename = null;
        if (filename == null) {
            final String var = br.getRegex("var fulllink.*?'\\+(.*?)\\+'").getMatch(0);
            filename = br.getRegex("'\\+" + var + "\\+'/(.*?)';").getMatch(0);
        }

        if (filename == null || filename.contains("/fileName?key=")) {
            String url = br.getRegex("document\\.location = \\'(/d/[^<>\"]*?\\';)").getMatch(0);
            if (url != null) {
                filename = new Regex(url, "d/\\d+/\\d+/([^<>\"]*?)\\';").getMatch(0);
            } else {
                url = br.getRegex("dlbutton'\\).href.*?=.*?\\+\"/(.*?)\"").getMatch(0);
                if (url != null) filename = url;
            }
        }

        if (filename == null) {
            filename = br.getRegex("\\+\"/(.*?)\";").getMatch(0);
        }
        if (filename == null) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        downloadLink.setName(Encoding.htmlDecode(filename.trim()));
        if (!br.containsHTML(">Share movie:")) {
            final String filesize = br.getRegex(Pattern.compile("Size:(\\s+)?</font>(\\s+)?<font style=.*?>(.*?)</font>", Pattern.CASE_INSENSITIVE)).getMatch(2);
            if (filesize != null) {
                downloadLink.setDownloadSize(SizeFormatter.getSize(filesize.replaceAll(",", "\\.")));
            }
        }
        return AvailableStatus.TRUE;
    }

    private String execJS(final String fun, final boolean fromFlash) throws Exception {
        Object result = new Object();
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("javascript");
        String[] docfn = new Regex(fun, "var a = document\\.getElementById\\(\'(.*?)\'\\)\\.getAttribute\\(\'(.*?)\'\\)").getRow(0);
        if (!fromFlash && docfn == null) return null;
        try {
            if (!fromFlash) {
                String value = br.getRegex("id=\"" + docfn[0] + "\" " + docfn[1] + "=\"(\\d+)\" ").getMatch(0);
                // document.getElementById('id').getAttribute('class')
                engine.eval("var document = { getElementById : function(a) { var newObj = new Object(); function getAttribute(b) { return " + value + "; } newObj.getAttribute = getAttribute; return newObj;}};");
            }
            result = ((Double) engine.eval(fun)).intValue();
        } catch (final Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (result == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        return result.toString();
    }

    @Override
    public String getAGBLink() {
        return "http://www.zippyshare.com/terms.html";
    }

    private int getHashfromFlash(final String flashurl, final int time) throws Exception {
        final String[] args = { "-abc", flashurl };
        // disassemble abc
        final String asasm = flash.swf.tools.SwfxPrinter.main(args);
        final String doABC = new Regex(asasm, "<doABC2>(.*)</doABC2>").getMatch(0);
        final String[] methods = new Regex(doABC, "(function.+?Traits Entries)").getColumn(0);
        String function = null;
        for (final String method : methods) {
            if (method.contains(":::break")) {
                for (final String lines : Regex.getLines(method)) {
                    if (lines.contains("pushbyte")) {
                        function = new Regex(lines, "\t(\\d+)").getMatch(0);
                    } else if (lines.contains("multiply")) {
                        function += "*" + time + "#";
                    } else if (lines.contains("pushint")) {
                        function += new Regex(lines, "\t(\\d+)\t").getMatch(0);
                    } else if (lines.contains("modulo") && function != null) {
                        function = function.replace("#", "%");
                    }
                }
                if (function == null) { return 0; }
            }
        }
        return Integer.parseInt(execJS(function, true));
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(true);
        setBrowserExclusive();
        prepareBrowser(downloadLink);
        final String mainpage = downloadLink.getDownloadURL().substring(0, downloadLink.getDownloadURL().indexOf(".com/") + 5);
        // DLLINK via packed JS or Flash App
        if (br.containsHTML("DownloadButton_v1\\.14s\\.swf")) {
            final String flashContent = br.getRegex("swfobject.embedSWF\\((.*?)\\)").getMatch(0);
            final String flashurl = mainpage + "swf/DownloadButton_v1.14s.swf";
            if (flashContent != null) {
                DLLINK = new Regex(flashContent, "url: '(.*?)'").getMatch(0);
                final String seed = new Regex(flashContent, "seed: (\\d+)").getMatch(0);
                if (DLLINK == null || seed == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
                int time = Integer.parseInt(seed);
                try {
                    time = getHashfromFlash(flashurl, time);
                } catch (final Throwable e) {
                    throw new PluginException(LinkStatus.ERROR_FATAL, "Developer Version of JD needed!");
                }
                if (time == 0) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
                DLLINK = DLLINK + "&time=" + time;
                // corrupted files?
                if (DLLINK.startsWith("nulldownload")) {
                    DLLINK = DLLINK.replaceAll("null", mainpage);
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE);
                }
            }
        } else if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) {
            final String rcID = br.getRegex("Recaptcha\\.create\\(\"([^<>\"/]*?)\"").getMatch(0);
            final String shortenCode = br.getRegex("shortencode: \\'(\\d+)\\'").getMatch(0);
            if (rcID == null || shortenCode == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(rcID);
            rc.load();
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            final String server = new Regex(br.getURL(), "(http://www\\d{0,}\\.zippyshare\\.com/)").getMatch(0);
            for (int i = 1; i <= 3; i++) {
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                br.postPage(server + "rest/captcha/test", "challenge=" + Encoding.urlEncode(rc.getChallenge()) + "&response=" + Encoding.urlEncode(c) + "&shortencode=" + shortenCode);
                if (br.toString().trim().equals("false")) {
                    rc.reload();
                    continue;
                }
                break;
            }
            if (br.toString().trim().equals("false")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            if (!br.toString().trim().equals("true")) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            DLLINK = server + "d/" + getDlCode(downloadLink.getDownloadURL()) + "/" + shortenCode + "/" + Encoding.urlEncode(downloadLink.getName());
        } else {
            DLLINK = br.getRegex("var fulllink = \\'(.*?)\\';").getMatch(0);
            if (DLLINK != null) {
                final String var = new Regex(DLLINK, "'\\+(.*?)\\+'").getMatch(0);
                String data = br.getRegex("var " + var + " = (.*?)\r?\n").getMatch(0);
                data = execJS(data, false);
                if (DLLINK.contains(var)) {
                    DLLINK = DLLINK.replace("'+" + var + "+'", data);
                }
            } else {
                DLLINK = br.getRegex("document\\.getElementById\\(\\'dlbutton\\'\\).href = \"/(.*?)\";").getMatch(0);
                String math = br.getRegex("\r?\n<script type=\"text/javascript\">(.*?)</script>\r?\n").getMatch(0);
                math = math.replaceAll("document\\.getElementById\\(\\'dlbutton\\'\\)\\.|\r?\n", "");
                if (DLLINK != null && math != null) {
                    final String var = new Regex(DLLINK, "\"\\+(.*?)\\+\"").getMatch(0);
                    final String varZ = new Regex(math, "\"\\+(.*?)\\+\"").getMatch(0);
                    if (varZ != null) {
                        math += varZ;
                    }
                    final String data = execJS(math, true);
                    if (DLLINK.contains(var)) {
                        DLLINK = DLLINK.replace("\"+" + var + "+\"", data);
                    }
                    DLLINK = mainpage + DLLINK;
                } else {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
            }
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, false, 1);
        downloadLink.setFinalFileName(Encoding.htmlDecode(getFileNameFromHeader(dl.getConnection())));
        if (!dl.getConnection().isContentDisposition()) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private void prepareBrowser(final DownloadLink downloadLink) throws IOException {
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        br.setCookie("http://www.zippyshare.com", "ziplocale", "en");
        br.getPage(downloadLink.getDownloadURL().replaceAll("locale=..", "locale=en"));
    }

    private String getDlCode(final String dlink) {
        String shortcode = new Regex(dlink, "zippyshare\\.com/v/(\\d+)").getMatch(0);
        if (shortcode == null) shortcode = new Regex(dlink, "(\\d+)$").getMatch(0);
        return shortcode;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {

    }

    @Override
    public void resetPluginGlobals() {
    }

}