//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "zaycev.net" }, urls = { "http://(www\\.)?zaycev\\.net/artist/\\d+(\\?page=\\d+)?" }, flags = { 0 })
public class ZyvNt extends PluginForDecrypt {

    public ZyvNt(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        final String artist = br.getRegex("<title>(.*?) - биография, онлайн биография, музыка</title>").getMatch(0);
        String[][] fileInfo = br.getRegex("<tr[^\r\n]+<a href=\"(/pages/\\d+/\\d+\\.shtml)\" itemprop=\"url audio\">(.*?)</span>[^\r\n]+<td class=\"size\">(.*?)</td>[^\r\n]+</tr>").getMatches();
        if (fileInfo == null || fileInfo.length == 0) {
            if (br.containsHTML(">Нет информации<")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (String[] file : fileInfo) {
            final DownloadLink dl = createDownloadlink("http://zaycev.net" + file[0]);
            dl.setFinalFileName(Encoding.htmlDecode(file[1].trim()).replaceAll("</?span[^>]+", "") + ".mp3");
            dl.setDownloadSize(SizeFormatter.getSize(file[2] + " MB"));
            dl.setAvailable(true);
            decryptedLinks.add(dl);
        }
        if (artist != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(artist.trim()));
            fp.addLinks(decryptedLinks);
            fp.setProperty("ALLOW_MERGE", true);
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}