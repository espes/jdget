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
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "4sex4.com" }, urls = { "http://(www\\.)?4sex4\\.com/\\d+/.*?\\.html" }, flags = { 0 })
public class FourSexFourCom extends PluginForDecrypt {

    public FourSexFourCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String tempID = br.getRegex("name=\"FlashVars\" value=\"options=(http://(www\\.)keezmovies\\.com/.*?)\"").getMatch(0);
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
        tempID = br.getRegex("name=\"FlashVars\" value=\"options=(http://(www\\.)?extremetube\\.com/embed_player\\.php\\?id=\\d+)\"").getMatch(0);
        if (tempID != null) {
            br.getPage(tempID);
            String finallink = br.getRegex("<click_tag>(http://.*?)</click_tag>").getMatch(0);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            DownloadLink dl = createDownloadlink(finallink);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("xvideos\\.com/embedframe/(\\d+)").getMatch(0);
        if (tempID == null) tempID = br.getRegex("fucking8\\.com/includes/player/\\?video=xv(\\d+)\"").getMatch(0);
        if (tempID == null) tempID = br.getRegex("\\?video=xv(\\d+)\\'").getMatch(0);
        if (tempID != null) {
            decryptedLinks.add(createDownloadlink("http://www.xvideos.com/video" + tempID));
            return decryptedLinks;
        }
        tempID = br.getRegex("emb\\.slutload\\.com/([A-Za-z0-9]+)\"").getMatch(0);
        if (tempID != null) {
            decryptedLinks.add(createDownloadlink("http://slutload.com/watch/" + tempID));
            return decryptedLinks;
        }
        tempID = br.getRegex("xhamster\\.com/xembed\\.php\\?video=(\\d+)\"").getMatch(0);
        if (tempID != null) {
            decryptedLinks.add(createDownloadlink("http://xhamster.com/movies/" + tempID + "/" + System.currentTimeMillis() + ".html"));
            return decryptedLinks;
        }
        // Filename needed for all sites below
        String filename = br.getRegex("<title>(.*?) \\- 4sex4\\.com</title>").getMatch(0);
        if (filename == null) filename = br.getRegex("<h1>(.*?)</h1>").getMatch(0);
        if (filename == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        filename = filename.trim();
        tempID = br.getRegex("shufuni\\.com/Flash/.*?flashvars=\"VideoCode=(.*?)\"").getMatch(0);
        if (tempID != null) {
            if (filename == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            DownloadLink dl = createDownloadlink("http://www.shufuni.com/handlers/FLVStreamingv2.ashx?videoCode=" + tempID);
            dl.setFinalFileName(Encoding.htmlDecode(filename.trim()));
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("flashvars=\"file=(http%3A%2F%2Fdownload\\.youporn\\.com[^<>\"]*?)\\&").getMatch(0);
        if (tempID != null) {
            br.getPage(Encoding.htmlDecode(tempID));
            if (br.getRequest().getHttpConnection().getResponseCode() == 404) {
                logger.warning("FourSexFourCom -> youporn link invalid, please check browser to confirm: " + parameter);
                return null;
            }
            if (br.containsHTML("download\\.youporn\\.com/agecheck")) {
                logger.info("Link broken or offline: " + parameter);
                return decryptedLinks;
            }
            String finallink = br.getRegex("<location>(http://.*?)</location>").getMatch(0);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            DownloadLink dl = createDownloadlink("directhttp://" + Encoding.htmlDecode(finallink));
            String type = br.getRegex("<meta rel=\"type\">(.*?)</meta>").getMatch(0);
            if (type == null) type = "flv";
            dl.setFinalFileName(filename + "." + type);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        if (tempID == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        return decryptedLinks;
    }

}
