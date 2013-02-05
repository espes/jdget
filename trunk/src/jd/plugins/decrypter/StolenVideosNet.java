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

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "stolenvideos.net" }, urls = { "http://(www\\.)?stolenvideos\\.net/\\d+/[A-Za-z0-9\\-_]+\\.html" }, flags = { 0 })
public class StolenVideosNet extends PluginForDecrypt {

    public StolenVideosNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        br.setFollowRedirects(false);
        String parameter = param.toString();
        br.getPage(parameter);
        String tempID = br.getRedirectLocation();
        String filename = br.getRegex("<title>Welcome to Stolen XXX Videos \\- Viewing Media \\-([^<>\"]*?)\\- Daily Free XXX Porn Videos</title>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>Viewing Media \\-([^<>\"]*?) \\- Welcome to Stolen XXX Videos \\- Daily Free XXX Porn Videos</title>").getMatch(0);
        if (tempID != null && tempID.length() < 40) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        if (tempID != null) {
            final DownloadLink dl = createDownloadlink(tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("\"file=(http://hosted\\.yourvoyeurvideos\\.com/videos/\\d+\\.flv)\\&").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://" + tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("\"http://(www\\.)?pornyeah\\.com/videos/[a-z0-9\\-]+\\-(\\d+)\\.html\"").getMatch(1);
        if (tempID == null) tempID = br.getRegex("pornyeah\\.com/playerConfig\\.php\\?[^<>\"]*?\\|(\\d+)\\|\\d+\"").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("http://www.pornyeah.com/videos/x-" + tempID + ".html");
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("<frame src=\"(http://(www\\.)?pornhost\\.com/\\d+/?)\"").getMatch(0);
        if (tempID != null) {
            final DownloadLink dl = createDownloadlink(tempID);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        // For all following ids, a filename is needed
        if (filename == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        filename = Encoding.htmlDecode(filename.trim());
        tempID = br.getRegex("\\&file=(http://[^<>\"]*?\\.flv)\\&").getMatch(0);
        if (tempID != null) {
            DownloadLink dl = createDownloadlink("directhttp://" + tempID);
            dl.setFinalFileName(filename + ".flv");
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        tempID = br.getRegex("<iframe id=\"preview\" src=\"(http://gallys\\.nastydollars\\.com/[^<>\"]+)").getMatch(0);
        if (tempID != null) {
            br.getPage(tempID);
            tempID = br.getRegex("<iframe src=\"(http://[^<>\"]+)").getMatch(0);
            if (tempID != null) {
                br.getPage(tempID);
                tempID = br.getRegex("<a href=\"(http://[^<>]+\\.flv)\" id=\"media\"").getMatch(0);
                if (tempID != null) {
                    final DownloadLink dl = createDownloadlink("directhttp://" + tempID);
                    dl.setFinalFileName(filename + ".flv");
                    decryptedLinks.add(dl);
                    return decryptedLinks;
                }
            }
        }
        logger.warning("Couldn't decrypt link: " + parameter);
        return decryptedLinks;
    }

}
