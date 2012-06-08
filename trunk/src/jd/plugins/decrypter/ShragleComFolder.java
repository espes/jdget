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

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "cloudnator.com" }, urls = { "http://(www\\.)?(shragle|cloudnator)\\.com/folder/[a-z0-9]+" }, flags = { 0 })
public class ShragleComFolder extends PluginForDecrypt {

    public ShragleComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("shragle.com/", "cloudnator.com/");
        br.setCookie("http://www.cloudnator.com/", "lang", "en_GB");
        br.getPage(parameter);
        if (br.containsHTML("(><b>An unknown error occured </b|>The selected folder was not found\\.<)")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        final String fpName = br.getRegex("<h2 class=\"box\\-title bold\">([^<>\"]*?)<span style=").getMatch(0);
        final String[] links = br.getRegex("\"(http://(www\\.)?cloudnator\\.com/files/[a-z0-9]+/.*?)\"").getColumn(0);
        if (links == null || links.length == 0) return null;
        for (String dl : links)
            decryptedLinks.add(createDownloadlink(dl));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpName.trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
