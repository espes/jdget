//jDownloader - Downloadmanager
//Copyright (C) 2011  JD-Team support@jdownloader.org
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

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "fickporno.net" }, urls = { "http://(www\\.)?fickporno\\.net/[a-z0-9\\-]+\\.html" }, flags = { 0 })
public class FickPornoNet extends PluginForDecrypt {

    public FickPornoNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    // This is a site which shows embedded videos of other sites so we may have
    // to add regexes/handlings here
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String filename = br.getRegex("<center><h2>(.*?)</h2><br>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>(.*?) fick porno</title>").getMatch(0);
        if (filename == null) {
            logger.warning("fickporno decrypter broken(filename regex) for link: " + parameter);
            return null;
        }
        filename = filename.trim();
        String externID = br.getRegex("value=\"options=(http://.*?)\"").getMatch(0);
        if (externID != null) {
            br.getPage(externID);
            if (br.containsHTML("No htmlCode read")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String finallink = br.getRegex("<flv_url>(http://.*?)</flv_url>").getMatch(0);
            if (finallink == null) {
                finallink = br.getRegex("<video_url>(<\\!\\[CDATA\\[)?(.*?)(\\]\\])?></video_url>").getMatch(1);
                if (finallink == null) {
                    logger.warning("Decrypter broken for link: " + parameter);
                    return null;
                }
            }

            final DownloadLink dl = createDownloadlink("directhttp://" + Encoding.htmlDecode(finallink));
            dl.setFinalFileName(filename + ".flv");
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        externID = br.getRegex("flashvars=\"file=(http.*?)http://static\\.youporn\\.com/r/").getMatch(0);
        if (externID != null) {
            br.getPage(Encoding.htmlDecode(externID));
            if (br.getRequest().getHttpConnection().getResponseCode() == 404) {
                logger.warning("fickporno -> youporn link invalid, please check browser to confirm: " + parameter);
                return null;
            }
            if (br.containsHTML("download\\.youporn\\.com/agecheck") || br.containsHTML("http\\-equiv=\"refresh\" content=\"\\d+;url=http://(www\\.)?download\\.youporn\\.com/")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String finallink = br.getRegex("<location>(http://.*?)</location>").getMatch(0);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            final DownloadLink dl = createDownloadlink("directhttp://" + Encoding.htmlDecode(finallink));
            String type = br.getRegex("<meta rel=\"type\">(.*?)</meta>").getMatch(0);
            if (type == null) type = "flv";
            dl.setFinalFileName(filename + "." + type);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        if (br.containsHTML("(holyxxx\\.com/|name=\"movie\" value=\"http://(www\\.)?pornotube\\.com/player/)")) {
            logger.info("This link is probably broken: " + parameter);
            return decryptedLinks;
        }
        logger.warning("Decrypter broken or link offline: " + parameter);
        return decryptedLinks;
    }

}
