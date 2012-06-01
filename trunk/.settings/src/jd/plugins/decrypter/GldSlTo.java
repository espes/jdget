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

@DecrypterPlugin(revision = "$Revision: 15186 $", interfaceVersion = 2, names = { "goldesel.to" }, urls = { "http://(www\\.)?goldesel\\.to/download/\\d+/.{1}" }, flags = { 0 })
public class GldSlTo extends PluginForDecrypt {

    public GldSlTo(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        // Important: Does not work without this cookie!
        br.setCookie("http://goldesel.to/", "goldesel_in", "1");
        br.getPage(parameter);
        String fpName = br.getRegex("class=\"content_box_head\">Detailansicht von \"(.*?)\"</div>").getMatch(0);
        if (fpName == null) {
            fpName = br.getRegex("width=\"14\" height=\"14\" align=\"absbottom\" />\\&nbsp;\\&nbsp;(.*?)</span><div").getMatch(0);
            if (fpName == null) {
                fpName = br.getRegex("style=\"float:left; margin:0px;\"><strong>\"(.*?)\"</strong>").getMatch(0);
                if (fpName == null) {
                    fpName = br.getRegex("<title>(.*?) \\(Download\\) \\- GoldEsel</title>").getMatch(0);
                }
            }
        }
        String[] decryptIDs = br.getRegex("onClick=\"window\\.open\\(\\'http://goldesel\\.to/dl/\\', \\'(\\d+)\\'").getColumn(0);
        if (decryptIDs == null || decryptIDs.length == 0) {
            decryptIDs = br.getRegex("goD\\(\\'(\\d+)\\'\\);").getColumn(0);
            if (decryptIDs == null || decryptIDs.length == 0) {
                decryptIDs = br.getRegex("href=\"http://goldesel\\.to/dl/\" target=\"(.*?)\"").getColumn(0);
            }
        }
        if (decryptIDs == null || decryptIDs.length == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        progress.setRange(decryptIDs.length);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        for (String cryptID : decryptIDs) {
            br.postPage("http://goldesel.to/ajax/go2dl.php", "Download=" + cryptID);
            String finallink = br.toString();
            if (!finallink.startsWith("http") || finallink.length() > 500) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            decryptedLinks.add(createDownloadlink(finallink));
            progress.increase(1);
        }
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
