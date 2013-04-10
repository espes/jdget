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

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision: 17642 $", interfaceVersion = 2, names = { "rapidgator.net" }, urls = { "http://(www\\.)?rapidgator\\.net/folder/\\d+/[\\w\\%]+" }, flags = { 0 })
public class RapidGatorNetFolder extends PluginForDecrypt {

    public RapidGatorNetFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        // standardise browser configurations to avoid detection.
        /* we first have to load the plugin, before we can reference it */
        JDUtilities.getPluginForHost("rapidgator.net");
        jd.plugins.hoster.RapidGatorNet.prepareBrowser(br);
        // normal stuff
        br.getPage(parameter);
        if (br.containsHTML("E_FOLDERNOTFOUND")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String fpName = br.getRegex("Downloading:[\r\n\t ]+</strong>[\r\n\t ]+(.*?)[\r\n\t ]+</p>").getMatch(0);
        if (fpName == null) fpName = br.getRegex("<title>Download file (.*?)</title>").getMatch(0);
        parsePage(decryptedLinks, parameter);
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpName.trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    private void parsePage(ArrayList<DownloadLink> ret, String parameter) {
        String[] links = br.getRegex("\"(/file/([a-z0-9]{32}|\\d+)/[^\"]+)").getColumn(0);

        if (links == null || links.length == 0) {
            logger.warning("Empty folder, or possible plugin defect. Please confirm this issue within your browser, if the plugin is truely broken please report issue to JDownloader Development Team. " + parameter);
            return;
        }
        if (links != null && links.length != 0) {
            for (String dl : links)
                ret.add(createDownloadlink("http://rapidgator.net" + dl));
        }

    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}