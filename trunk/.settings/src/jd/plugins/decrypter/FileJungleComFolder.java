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
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 14964 $", interfaceVersion = 2, names = { "filejungle.com" }, urls = { "http://(www\\.)?filejungle\\.com/l/[A-Za-z0-9]+" }, flags = { 0 })
public class FileJungleComFolder extends PluginForDecrypt {

    public FileJungleComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML("The URL you entered cannot be found on the server")) return decryptedLinks;
        String fpName = br.getRegex("class=\"folder\"><span>\\&nbsp;</span>(.*?)</div>").getMatch(0);
        if (fpName == null) fpName = br.getRegex("Folder name:([^/\"]+)(</b>|\\[/b\\]\\[/url\\])\"").getMatch(0);
        String[] links = br.getRegex("<div id=\"file_name\">[\t\n\r ]+<a href=\"(http://.*?)\"").getColumn(0);
        if (links == null || links.length == 0) links = br.getRegex("\"(http://(www\\.)?filejungle\\.com/f/[A-Za-z0-9]+)\"").getColumn(0);
        if (links == null || links.length == 0) {
            if (fpName != null) return decryptedLinks;
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (String dl : links)
            decryptedLinks.add(createDownloadlink(dl));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
