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

@DecrypterPlugin(revision = "$Revision: 15600 $", interfaceVersion = 2, names = { "wastedamateurs.com" }, urls = { "http://(www\\.)?wastedamateurs\\.com/\\d+/.*?\\.html" }, flags = { 0 })
public class WastedAmateursCom extends PluginForDecrypt {

    public WastedAmateursCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        br.setFollowRedirects(false);
        String parameter = param.toString();
        br.getPage(parameter);
        String tempID = br.getRedirectLocation();
        if (tempID != null) {
            DownloadLink dl = createDownloadlink(tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        String filename = br.getRegex(":: Viewing Media \\- (.*?)</title>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<meta name=\"title\" content=\"(.*?)\" />").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("target=\"_blank\" title=\"Click to Download FULL VIDEO and Get Access to our Huge Video Archives\">(.*?) </a></h1>").getMatch(0);
            }
        }
        if (filename == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        filename = filename.trim();
        tempID = br.getRegex("\\'(http://(www\\.)?myxvids\\.com/embed_code/\\d+/\\d+/myxvids_embed\\.js)\\'").getMatch(0);
        if (tempID != null) {
            br.getPage(tempID);
            String finallink = br.getRegex("var urlAddress = \"(http://.*?)\"").getMatch(0);
            if (finallink == null) {
                finallink = br.getRegex("\"linkUrl\":\"(http://.*?)\"").getMatch(0);
                if (finallink == null) {
                    finallink = br.getRegex("<b>PAGE URL:</b><br>\\[\t\n\r ]+<a href=\"(http://.*?)\"").getMatch(0);
                    if (finallink == null) {
                        finallink = br.getRegex("\" style=\"color: #FFFFFF\">(http://.*?)</a>").getMatch(0);
                    }
                }
            }
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            DownloadLink dl = createDownloadlink(finallink);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("http://flash\\.serious\\-cash\\.com/flvplayer\\.swf\" width=\"\\d+\" height=\"\\d+\" allowfullscreen=\"true\" flashvars=\"file=(.*?)\\&").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://http://flash.serious-cash.com/" + tempID + ".flv");
            decryptedLinks.add(dl);
            dl.setFinalFileName(filename + ".flv");
            return decryptedLinks;
        }
        tempID = br.getRegex("file=(http://(www\\.)?hostave\\d+\\.net/.*?)\\&screenfile").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://" + tempID);
            dl.setFinalFileName(filename + ".flv");
            decryptedLinks.add(dl);
            return decryptedLinks;

        }
        tempID = br.getRegex("var urlAddress = \"(http://.*?)\"").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink(tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("\\&file=(http://static\\.mofos\\.com/.*?)\\&enablejs").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://" + tempID);
            dl.setFinalFileName(filename + ".flv");
            decryptedLinks.add(dl);
            return decryptedLinks;

        }
        tempID = br.getRegex("addVariable\\(\\'file\\',\\'(http://.*?)\\'\\)").getMatch(0);
        if (tempID == null) tempID = br.getRegex("\\'(http://(www\\.)?amateurdumper\\.com/videos/.*?)\\'").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://" + tempID);
            dl.setFinalFileName(filename + ".flv");
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
