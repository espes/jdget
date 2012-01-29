//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
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
import java.util.HashMap;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.RequestHeader;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filesavr.com" }, urls = { "http://[\\w\\.]*?filesavr\\.com/[A-Za-z0-9]+(_\\d+)?" }, flags = { 0 })
public class FileSavrCom extends PluginForHost {

    private Browser browser;

	public FileSavrCom(PluginWrapper wrapper) {
        super(wrapper);
        
        this.browser = new Browser();
		
		RequestHeader rh = browser.getHeaders();
        HashMap<String, String> headers = new HashMap<String, String>();
        for (int i=0; i<rh.size(); i++)
        	headers.put(rh.getKey(i), rh.getValue(i));
        headers.put("User-Agent", "");
        browser.setHeaders(new RequestHeader(headers));
    }

    @Override
    public String getAGBLink() {
        return "http://www.filesavr.com/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
    	requestFileInformation(downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(browser, downloadLink, "http://www.filesavr.com/download/do_download", "key=" + new Regex(downloadLink.getDownloadURL(), "filesavr\\.com/(.+)").getMatch(0), false, 1);
        if (dl.getConnection().getResponseCode() != 200 && dl.getConnection().getResponseCode() != 206) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 30 * 1000l);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
    	this.setBrowserExclusive();
        browser.getPage(link.getDownloadURL());
        if (browser.containsHTML("Sorry, File not found\\!")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = browser.getRegex("class=\"file\\-name\\-text\">(.*?)<").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(filename.trim());
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

}