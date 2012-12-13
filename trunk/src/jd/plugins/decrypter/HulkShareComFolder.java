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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "hulkshare.com" }, urls = { "http://(www\\.)?(hulkshare\\.com|hu\\.lk)/(?!dl/)[a-z0-9]+" }, flags = { 0 })
public class HulkShareComFolder extends PluginForDecrypt {

    public HulkShareComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String HULKSHAREDOWNLOADLINK = "http://(www\\.)?(hulkshare\\.com|hu\\/lk)/[a-z0-9]{12}";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString().replaceFirst("hu\\.lk/", "hulkshare\\.com/");
        br.setFollowRedirects(false);
        br.setCookie("http://hulkshare.com/", "lang", "english");
        br.getPage(parameter);
        String argh = br.getRedirectLocation();
        if (br.containsHTML("class=\"bigDownloadBtn") || argh != null) {
            decryptedLinks.add(createDownloadlink(parameter.replace("hulkshare.com/", "hulksharedecrypted.com/")));
            return decryptedLinks;
        }
        if (br.containsHTML("You have reached the download\\-limit")) {
            final DownloadLink dl = createDownloadlink(parameter.replace("hulkshare.com/", "hulksharedecrypted.com/"));
            dl.setAvailable(false);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        if (br.containsHTML(">Page not found") || br.containsHTML(">This file has been subject to a DMCA notice")) {
            final DownloadLink dl = createDownloadlink(parameter.replace("hulkshare.com/", "hulksharedecrypted.com/"));
            dl.setProperty("fileoffline", true);
            dl.setAvailable(false);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        final String uid = br.getRegex("\\?uid=(\\d+)").getMatch(0);
        if (uid == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        final String internalFolderlink = "http://www.hulkshare.com/userPublic.php?uid=" + uid + "&fld_id=0&per_page=150&page=";
        br.getPage(internalFolderlink + "1");
        ArrayList<String> pages = new ArrayList<String>();
        pages.add("1");
        final String[] tempPages = br.getRegex("\\&page=(\\d+)\"").getColumn(0);
        if (tempPages != null && tempPages.length != 0) {
            for (final String tempPage : tempPages)
                if (!pages.contains(tempPage)) pages.add(tempPage);
        }
        for (final String currentPage : pages) {
            if (!currentPage.equals("1")) {
                br.getPage(internalFolderlink + currentPage);
                if (br.containsHTML("This user has no tracks")) break;
            }
            final String[] links = br.getRegex("<a href=\"(http://(www\\.)?hulkshare\\.com/[a-z0-9]{12})\"").getColumn(0);
            if (links == null || links.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            for (String singleLink : links)
                decryptedLinks.add(createDownloadlink(singleLink.replace("hulkshare.com/", "hulksharedecrypted.com/")));
        }
        final FilePackage fp = FilePackage.getInstance();
        fp.setName(new Regex(parameter, "hulkshare\\.com/(.+)").getMatch(0));
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

}
