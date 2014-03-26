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

import java.io.IOException;
import java.util.Random;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "pcloud.com" }, urls = { "http://pclouddecrypted\\.com/\\d+" }, flags = { 2 })
public class PCloudCom extends PluginForHost {

    public PCloudCom(PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
    }

    @Override
    public String getAGBLink() {
        return "https://my.pcloud.com/#page=policies&tab=terms-of-service";
    }

    private static final String TYPE_OLD     = "https?://(www\\.)?(my\\.pcloud\\.com/#page=publink\\&code=|pc\\.cd/)[A-Za-z0-9]+";

    private static final String DOWNLOAD_ZIP = "DOWNLOAD_ZIP_2";

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        /* Links before big change */
        if (link.getDownloadURL().matches(TYPE_OLD))
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        else if (link.getBooleanProperty("offline", false)) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        this.setBrowserExclusive();
        prepBR();
        final String code = link.getStringProperty("plain_code", null);
        final String fileid = link.getStringProperty("plain_fileid", null);
        if (isCompleteFolder(link)) {
            br.getPage("http://api.pcloud.com/showpublink?code=" + code);
            /* 7002 = deleted by the owner */
            if (br.containsHTML("\"error\": \"Invalid link") || br.containsHTML("\"result\": 7002")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else {
            br.getPage("https://api.pcloud.com/getpublinkdownload?code=" + code + "&forcedownload=1&fileid=" + fileid);
            if (br.containsHTML("\"error\": \"Invalid link \\'code\\'\\.\"|\"error\": \"File not found\\.\"")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        final String filename = link.getStringProperty("plain_name", null);
        final String filesize = link.getStringProperty("plain_size", null);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setFinalFileName(filename);
        link.setDownloadSize(Long.parseLong(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        final String dllink = getdllink(downloadLink);
        boolean resume = true;
        int maxchunks = 0;
        if (isCompleteFolder(downloadLink)) {
            resume = false;
            maxchunks = 1;
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, resume, maxchunks);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String getJson(final String parameter) {
        String result = br.getRegex("\"" + parameter + "\": (\\d+)").getMatch(0);
        if (result == null) result = br.getRegex("\"" + parameter + "\": \"([^<>\"]*?)\"").getMatch(0);
        return result;
    }

    private String getdllink(final DownloadLink dl) throws PluginException {
        String dllink = null;
        if (isCompleteFolder(dl)) {
            /* Select all IDs of the folder to download all as .zip */
            final String[] fileids = br.getRegex("\"fileid\": (\\d+)").getColumn(0);
            if (fileids == null || fileids.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            dllink = "https://api.pcloud.com/getpubzip?fileids=";
            for (int i = 0; i < fileids.length; i++) {
                final String currentID = fileids[i];
                if (i == fileids.length - 1) {
                    dllink += currentID;
                } else {
                    dllink += currentID + "%2C";
                }
            }
            dllink += "&filename=" + dl.getStringProperty("plain_name", null) + "&code=" + dl.getStringProperty("plain_code", null);
        } else {
            final String hoststext = br.getRegex("\"hosts\": \\[(.*?)\\]").getMatch(0);
            if (hoststext == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final String[] hosts = new Regex(hoststext, "\"([^<>\"]*?)\"").getColumn(0);
            dllink = getJson("path");
            if (dllink == null || hosts == null || hosts.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            dllink = dllink.replace("\\", "");
            dllink = "https://" + hosts[new Random().nextInt(hosts.length - 1)] + dllink;
        }
        return dllink;
    }

    private boolean isCompleteFolder(final DownloadLink dl) {
        return dl.getBooleanProperty("complete_folder", false);
    }

    private void prepBR() {
        br.setFollowRedirects(true);
        br.getHeaders().put("Accept-Encoding", "gzip");
        br.getHeaders().put("Accept", "application/json, text/javascript, */*; q=0.01");
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getHeaders().put("Accept-Language", "en-us;q=0.7,en;q=0.3");
        br.getHeaders().put("Accept-Charset", null);
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), PCloudCom.DOWNLOAD_ZIP, JDL.L("plugins.hoster.PCloudCom.DownloadZip", "Download .zip file of all files in the folder?")).setDefaultValue(false));
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