//  jDownloader - Downloadmanager
//  Copyright (C) 2013  JD-Team support@jdownloader.org
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.Browser.BrowserException;
import jd.nutils.encoding.HTMLEntities;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "docs.google.com" }, urls = { "https?://(www\\.)?(docs|drive)\\.google\\.com/((leaf|open|uc)\\?([^<>\"/]+)?id=[A-Za-z0-9\\-_]+|file/d/[A-Za-z0-9\\-_]+)" }, flags = { 0 })
public class DocsGoogleCom extends PluginForHost {

    public DocsGoogleCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "https://support.google.com/drive/bin/answer.py?hl=en_GB&answer=2450387";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    public void correctDownloadLink(DownloadLink link) throws PluginException {
        String id = getID(link);
        if (id == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        } else {
            link.setUrlDownload("https://docs.google.com/file/d/" + id);
        }
    }

    private static final String NOCHUNKS     = "NOCHUNKS";
    private boolean             pluginloaded = false;

    private String getID(DownloadLink downloadLink) {
        // known url formats
        // https://docs.google.com/file/d/0B4AYQ5odYn-pVnJ0Z2V4d1E5UWc/preview?pli=1
        // can't dl these particular links, same with document/doc, presentation/present and view
        // https://docs.google.com/uc?id=0B4AYQ5odYn-pVnJ0Z2V4d1E5UWc&export=download
        // https://docs.google.com/leaf?id=0B_QJaGmmPrqeZjJkZDFmYzEtMTYzMS00N2Y2LWI2NDUtMjQ1ZjhlZDhmYmY3
        // https://docs.google.com/open?id=0B9Z2XD2XD2iQNmxzWjd1UTdDdnc

        if (downloadLink == null) {
            return null;
        }
        String id = new Regex(downloadLink.getDownloadURL(), "/file/d/([a-zA-Z0-9\\-_]+)").getMatch(0);
        if (id == null) {
            id = new Regex(downloadLink.getDownloadURL(), "(?!rev)id=([a-zA-Z0-9\\-_]+)").getMatch(0);
        }
        return id;
    }

    public String agent = null;

    public Browser prepBrowser(Browser pbr) {
        // used within the decrypter also, leave public
        // language determined by the accept-language
        // user-agent required to use new ones otherwise blocks with javascript notice.

        if (pbr == null) {
            pbr = new Browser();
        }
        if (agent == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            agent = jd.plugins.hoster.MediafireCom.stringUserAgent();
        }
        pbr.getHeaders().put("User-Agent", agent);
        pbr.getHeaders().put("Accept-Language", "en-gb, en;q=0.9");
        pbr.setCustomCharset("utf-8");
        pbr.setFollowRedirects(true);
        return pbr;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        prepBrowser(br);
        try {
            br.getPage("https://docs.google.com/leaf?id=" + getID(link));
        } catch (final BrowserException e) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (br.containsHTML("<p class=\"error\\-caption\">Sorry, we are unable to retrieve this document\\.</p>")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String filename = br.getRegex("'title': '([^<>\"]*?)'").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("\"filename\":\"([^\"]+)\",").getMatch(0);
        }
        final String size = br.getRegex("\"sizeInBytes\":(\\d+),").getMatch(0);
        if (filename == null) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        link.setName(filename.trim());
        if (size != null) {
            link.setVerifiedFileSize(Long.parseLong(size));
            link.setDownloadSize(SizeFormatter.getSize(size));
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        br.setFollowRedirects(false);
        String dllink = null;
        String streamLink = null;
        /* Download not possible ? Download stream! */
        final String stream_map = br.getRegex("\"fmt_stream_map\":\"(.*?)\"").getMatch(0);
        if (stream_map != null) {
            final String[] links = stream_map.split("\\|");
            streamLink = links[links.length - 1];
            streamLink = unescape(streamLink);
        }

        br.getPage("https://docs.google.com/uc?id=" + getID(downloadLink) + "&export=download");
        if (br.containsHTML("<p class=\"error\\-subcaption\">Too many users have viewed or downloaded this file recently\\. Please try accessing the file again later\\.")) {
            // so its not possible to download at this time.
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Download not possible at this point in time.", 60 * 60 * 1000);
        }
        dllink = br.getRedirectLocation();
        if (dllink == null) {
            dllink = br.getRegex("href=\"([^\"]+)\">Download anyway</a>").getMatch(0);
            if (dllink == null) {
                dllink = br.getRegex("href=\"(/uc\\?export=download[^\"]+)\">").getMatch(0);
                if (dllink != null) {
                    dllink = HTMLEntities.unhtmlentities(dllink);
                }
            } else {
                br.getPage(HTMLEntities.unhtmlentities(dllink));
                dllink = br.getRedirectLocation();
            }
        }
        if (dllink == null && streamLink == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dllink == null) {
            dllink = streamLink;
        }
        int maxChunks = 0;
        if (downloadLink.getBooleanProperty(DocsGoogleCom.NOCHUNKS, false)) {
            maxChunks = 1;
        }
        br.setFollowRedirects(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, maxChunks);
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
                if (downloadLink.getBooleanProperty(DocsGoogleCom.NOCHUNKS, false) == false) {
                    downloadLink.setProperty(DocsGoogleCom.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            }
        } catch (final PluginException e) {
            // New V2 errorhandling
            /* unknown error, we disable multiple chunks */
            if (e.getLinkStatus() != LinkStatus.ERROR_RETRY && downloadLink.getBooleanProperty(DocsGoogleCom.NOCHUNKS, false) == false) {
                downloadLink.setProperty(DocsGoogleCom.NOCHUNKS, Boolean.valueOf(true));
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw e;
        }
    }

    private String unescape(final String s) {
        /* we have to make sure the youtube plugin is loaded */
        if (pluginloaded == false) {
            final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
            if (plugin == null) {
                throw new IllegalStateException("youtube plugin not found!");
            }
            pluginloaded = true;
        }
        return jd.plugins.hoster.Youtube.unescape(s);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}