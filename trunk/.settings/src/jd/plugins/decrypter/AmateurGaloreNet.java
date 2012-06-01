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
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 15600 $", interfaceVersion = 2, names = { "amateurgalore.net" }, urls = { "http://(www\\.)?amateurgalore\\.net/index/video/[a-z0-9_\\-]+" }, flags = { 0 })
public class AmateurGaloreNet extends PluginForDecrypt {

    public AmateurGaloreNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String tempID = br.getRegex("\"http://videobam\\.com/widget/(.*?)/custom").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("http://videobam.com/videos/download/" + tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("name=\"FlashVars\" value=\"options=(http://(www\\.)keezmovies\\.com/.*?)\"").getMatch(0);
        if (tempID != null) {
            br.getPage(tempID);
            String finallink = br.getRegex("<share>(http://.*?)</share>").getMatch(0);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            DownloadLink dl = createDownloadlink(finallink);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("movie_id=(\\d+)").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("http://www.pornrabbit.com/" + tempID + "/bla.html");
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("id_video=(\\d+)\"").getMatch(0);
        if (tempID == null) tempID = br.getRegex("xvideos\\.com/embedframe/(\\d+)\"").getMatch(0);
        if (tempID != null) {
            decryptedLinks.add(createDownloadlink("http://www.xvideos.com/video" + tempID));
            return decryptedLinks;
        }
        tempID = br.getRegex("(megaporn|cum)\\.com/e/([A-Z0-9]{8})").getMatch(1);
        if (tempID != null) {
            decryptedLinks.add(createDownloadlink("http://www.cum.com/video/?v=" + tempID));
            return decryptedLinks;
        }
        if (tempID == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        return decryptedLinks;
    }

}
