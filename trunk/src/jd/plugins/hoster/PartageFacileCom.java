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

import java.io.IOException;

import jd.PluginWrapper;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "partage-facile.com" }, urls = { "http://[\\w\\.]*?partage\\-facile\\.com/(?!forum|connexion|mentions|inscription|css|js)([0-9A-Z]+/.+|\\d+.*?\\.html)" }, flags = { 0 })
public class PartageFacileCom extends PluginForHost {

    public PartageFacileCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.partage-facile.com/cgu.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML(">Page introuvable<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML("http\\-equiv=\"refresh\" content=\"0;url=http://(www\\.)?partage\\-facile\\.com\"")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filesize = br.getRegex("<th>Taille :</th>[\t\n\r ]+<td>([^<>\"\\']+)</td>").getMatch(0);
        String filename = br.getRegex("<th class=\"span4\">Nom :</th>[\t\n\r ]+<td>([^<>\"\\']+)</td>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<input type=\"hidden\" name=\"nom\" value=\"([^<>\"\\']+)\"").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("Partage-Facile : Fichier ([^<>\"\\']+)\\.html").getMatch(0);
            }
        }
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (filesize.contains("Mo")) {
            filesize = filesize.replaceAll("Mo", "MB");
        } else if (filesize.contains("Go")) {
            filesize = filesize.replaceAll("Go", "GB");
        } else if (filesize.contains("Ko")) {
            filesize = filesize.replaceAll("Ko", "KB");
        } else if (filesize.contains("oct")) {
            filesize = filesize.replaceAll("oct", "b");
        } else {
            filesize = null;
        }
        downloadLink.setName(filename.trim());
        downloadLink.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        br.postPage(downloadLink.getDownloadURL(), "freedl=T%C3%A9l%C3%A9chargement+gratuit+%28Vitesse+NORMAL%29");
        Form dlform = br.getForm(0);
        if (dlform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dlform.remove("u");
        br.setFollowRedirects(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dlform, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("Taille maximum du fichier")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 30 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public void init() {
        br.setFollowRedirects(true);
        br.setRequestIntervalLimit(getHost(), 500);
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