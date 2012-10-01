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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "music.aol.com" }, urls = { "http://(www\\.)?music\\.aol\\.com/[a-z0-9\\-]+#/\\d+" }, flags = { 0 })
public class MusicAolCom extends PluginForDecrypt {

    public MusicAolCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br.setFollowRedirects(false);
        br.getPage(parameter);
        final int cdNum = Integer.parseInt(new Regex(parameter, "(\\d+)").getMatch(0)) - 1;
        final String feedName = br.getRegex("id=\"album_" + cdNum + "\" playlisturl=\"http://feeds\\.castfire\\.com/cdlp/all/aol/pls:([^<>\"]*?)\"").getMatch(0);
        if (feedName == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        br.getPage("http://feeds.castfire.com/cdlp/all/aol/pls:" + feedName + "?track1=stream&track2=Web+App&track3=AOL+Music+Web&callback=&_=" + System.currentTimeMillis());
        String albumName = br.getRegex("\"album_name\":\"([^<>\"]*?)\"").getMatch(0);
        final String[][] content = br.getRegex("\"title\":\"([^<>\"]*?)\",\"url\":\"(http:[^<>\"]*?)\"").getMatches();
        if (content == null || content.length == 0 || albumName == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        albumName = Encoding.htmlDecode(albumName);
        for (final String scontent[] : content) {
            br.getPage(Encoding.htmlDecode(scontent[1].replace("\\", "")));
            final String finallink = br.getRedirectLocation();
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            final DownloadLink dl = createDownloadlink("directhttp://" + finallink);
            dl.setFinalFileName(albumName + " - " + Encoding.htmlDecode(scontent[0]) + ".flv");
            decryptedLinks.add(dl);
        }
        final FilePackage fp = FilePackage.getInstance();
        fp.setName(albumName);
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

}
