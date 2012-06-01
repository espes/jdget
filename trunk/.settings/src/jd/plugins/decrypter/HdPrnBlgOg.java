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

import java.util.ArrayList;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision: 11709 $", interfaceVersion = 2, names = { "hd-pornblog.org" }, urls = { "http://hd\\-pornblog\\.org/\\?p=\\d+" }, flags = { 0 })
public class HdPrnBlgOg extends PluginForDecrypt {

    /**
     * @author OhGod + raztoki
     */

    // Dev notes
    // only grabs lower qual stuff. (designed in that manner)

    public HdPrnBlgOg(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String contentReleaseName = br.getRegex("<h1 class=\"entry\\-title\">(.*?)</h1>").getMatch(0);
        String contentReleaseLinks = br.getRegex("(/images/Download\\.png\".*images/HD720p\\.png\")").getMatch(0);
        if (contentReleaseLinks == null) {
            logger.warning("contentReleaeLinks == null");
            return null;
        }
        String[] links = new Regex(contentReleaseLinks, "<a href=\"(http[^\"]+)", Pattern.CASE_INSENSITIVE).getColumn(0);
        if (links == null || links.length == 0) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        for (String link : links) {
            decryptedLinks.add(createDownloadlink(link));
        }
        // assuming that this img hoster is used exclusively.
        String[] imgs = br.getRegex("(http://([\\w\\.]+)?fastpic\\.ru/thumb/[^\"]+)").getColumn(0);
        if (links != null && links.length != 0) {
            for (String img : imgs) {
                decryptedLinks.add(createDownloadlink("directhttp://" + img));
            }
        }

        if (contentReleaseName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(contentReleaseName).trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}