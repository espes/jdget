//jDownloader - Downloadmanager
//Copyright (C) 2012  JD-Team support@jdownloader.org
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

import java.io.IOException;

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "qq.com" }, urls = { "http://qqdecrypted\\.com/\\d+" }, flags = { 0 })
public class QqCom extends PluginForHost {

    public QqCom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.qq.com/contract.shtml";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    private static final String DECRYPTEDLINK = "http://qqdecrypted\\.com/\\d+";

    private static final String NOCHUNKS      = "NOCHUNKS";

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        if (link.getBooleanProperty("offline", false)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCustomCharset("utf-8");
        br.getPage(link.getStringProperty("mainlink", null));
        if (br.containsHTML(">很抱歉，此资源已被删除或包含敏感信息不能查看啦<")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        final String originalqhref = link.getStringProperty("qhref", null);
        final String[] qhrefs = br.getRegex("qhref=\"(qqdl://[^<>\"]+)\"").getColumn(0);
        if (qhrefs == null || qhrefs.length == 0) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        boolean failed = true;
        for (final String currentqhref : qhrefs) {
            if (currentqhref.equals(originalqhref)) {
                failed = false;
                break;
            }
        }
        if (failed) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        final String hash = downloadLink.getStringProperty("filehash", null);
        // This should never happen
        if (hash == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.getHeaders().put("User-Agent", "Mozilla/4.0 (compatible; MSIE 9.11; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
        br.postPage("http://fenxiang.qq.com/upload/index.php/share/handler_c/getComUrl", "filename=" + Encoding.urlEncode(downloadLink.getName()) + "&filehash=" + hash);
        if (br.containsHTML("No htmlCode read")) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 5 * 60 * 1000l);
        }
        String finallink = br.getRegex("\"com_url\":\"(htt[^<>\"]*?)\"").getMatch(0);
        final String cookie = br.getRegex("\"com_cookie\":\"([^<>\"]*?)\"").getMatch(0);
        if (finallink == null || cookie == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        final String finalhost = new Regex(finallink, "(https?://[A-Za-z0-9\\-\\.]+)(:|/)").getMatch(0);
        br.setCookie(finalhost, "FTN5K", cookie);

        int maxChunks = 0;
        if (downloadLink.getBooleanProperty(NOCHUNKS, false)) {
            maxChunks = 1;
        }

        /* Get better speeds - source: https://github.com/rhyzx/xuanfeng-userscript/blob/master/xuanfeng.user.js */
        finallink = finallink.replace("xflx.store.cd.qq.com:443", "xfcd.ctfs.ftn.qq.com");
        finallink = finallink.replace("xflx.sz.ftn.qq.com:80", "sz.disk.ftn.qq.com");
        finallink = finallink.replace("xflx.cd.ftn.qq.com:80", "cd.ctfs.ftn.qq.com");
        finallink = finallink.replace("xflxsrc.store.qq.com:443", "xfxa.ctfs.ftn.qq.com");

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, finallink, true, maxChunks);

        if (dl.getConnection().getResponseCode() == 503) {
            if (dl.getConnection().getResponseMessage().equals("Service Unavailable")) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, " Service Unavailable!");
            }
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 10 * 60 * 1000l);
        }
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        try {
            if (!this.dl.startDownload()) {
                try {
                    if (dl.externalDownloadStop()) {
                        return;
                    }
                } catch (final Throwable e) {
                }
                /* unknown error, we disable multiple chunks */
                if (downloadLink.getBooleanProperty(QqCom.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(QqCom.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            }
        } catch (final PluginException e) {
            // New V2 errorhandling
            /* unknown error, we disable multiple chunks */
            if (e.getLinkStatus() != LinkStatus.ERROR_RETRY && downloadLink.getBooleanProperty(QqCom.NOCHUNKS, false) == false) {
                downloadLink.setProperty(QqCom.NOCHUNKS, Boolean.valueOf(true));
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw e;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}