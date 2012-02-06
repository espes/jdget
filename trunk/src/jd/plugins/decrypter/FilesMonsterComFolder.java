//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
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

package jd.plugins.decrypter;

import java.io.IOException;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filesmonster.comFolder" }, urls = { "http://(www\\.)?filesmonster\\.com/folders\\.php\\?fid=([0-9a-zA-Z_-]{22}|\\d+)" }, flags = { 0 })
public class FilesMonsterComFolder extends PluginForDecrypt {

    public FilesMonsterComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setReadTimeout(3 * 60 * 1000);
        br.setFollowRedirects(false);
        br.setCookiesExclusive(true);
        br.getPage(parameter);
        if (br.containsHTML(">Folder does not exist<")) {
            logger.warning("Invalid URL: " + parameter);
            return decryptedLinks;
        }

        String fpName = br.getRegex(">Folder: (.*?)</span>").getMatch(0);

        parsePage(decryptedLinks);
        parseNextPage(decryptedLinks, parameter);

        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpName.trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    private void parsePage(ArrayList<DownloadLink> ret) {
        String[] links = br.getRegex("<a class=\"green\" href=\"(http://[\\w\\.\\d]*?filesmonster\\.com/.*?)\">").getColumn(0);
        if (links == null || links.length == 0) return;
        if (links != null && links.length != 0) {
            for (String dl : links)
                ret.add(createDownloadlink(dl));
        }
    }

    private boolean parseNextPage(ArrayList<DownloadLink> ret, String parameter) throws IOException {
        String firstpanel = new Regex(parameter, "</div>(.*?)<table class=\"folder\\_files\"").getMatch(0);
        String nextPage[] = new Regex(firstpanel, "\\&nbsp\\;<a href=\\'(folders.php\\?fid=.*?)\\'").getColumn(0);
        if (nextPage == null || nextPage.length == 0) return false;
        if (nextPage != null && nextPage.length != 0) {
            for (String page : nextPage) {
                br.getPage("http://filesmonster.com/" + page);
                parsePage(ret);
                return true;
            }
        }
        return false;
    }
}
