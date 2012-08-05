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
import jd.http.Browser;
import jd.http.RandomUserAgent;
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "gametrailers.com" }, urls = { "http://(www\\.)?gametrailers\\.com/((video|user\\-movie)/[\\w\\-]+/\\d+|(full\\-episodes|videos)/\\w+/[\\w\\-]+)" }, flags = { 0 })
public class GameTrailersCom extends PluginForDecrypt {
    private static final String ua = RandomUserAgent.generate();

    public GameTrailersCom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(final CryptedLink param, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br.getHeaders().put("User-Agent", ua);

        br.setFollowRedirects(true);
        br.getPage(parameter);

        String videoTitle = br.getRegex("<title>(.*?)\\s?\\|\\s?Gametrailers</title>").getMatch(0);
        if (videoTitle == null) {
            videoTitle = br.getRegex("name=\"title\" content=\"(.*?)\\s?\\|\\s?Gametrailers\"").getMatch(0);
        }

        String contentId = br.getRegex("data\\-contentId=(\'|\")([^\'\"]+)").getMatch(1);
        if (contentId == null) return decryptedLinks;
        Browser br2 = br.cloneBrowser();
        /* episodes */
        if (parameter.contains("/full-episodes/")) {
            br2.getPage("/feeds/mrss?uri=" + Encoding.urlEncode(contentId));
            String[] contentIds = br2.getRegex("<guid isPermaLink=\"(true|false)\">(.*?)</guid>").getColumn(1);
            String episodesTitle = br2.getRegex("<media:title><\\!\\[CDATA\\[(.*?)\\]\\]></media:title>").getMatch(0);
            if (contentIds == null || contentIds.length == 0) return decryptedLinks;
            int i = 1;
            for (String cId : contentIds) {
                br2.getPage("/feeds/mediagen/?uri=" + Encoding.urlEncode(cId) + "&forceProgressive=true");
                String link = br2.getRegex("<src>(http://.*?)</src>").getMatch(0);
                if (link == null) continue;
                DownloadLink dl = createDownloadlink("directhttp://" + link);
                dl.setFinalFileName(getVideoTitle(episodesTitle + "_Part" + i++));
                decryptedLinks.add(dl);
            }
            if (videoTitle.startsWith(" |")) {
                String tmpVideoTitle = br.getRegex("<meta itemprop=\"name\" content=\"([^\"]+)").getMatch(0);
                videoTitle = tmpVideoTitle != null ? tmpVideoTitle + videoTitle : "unknownTitle" + videoTitle;
            }
        } else {
            /* single video file */
            br2.getPage("/feeds/mediagen/?uri=" + Encoding.urlEncode(contentId) + "&forceProgressive=true");
            String link = br2.getRegex("<src>(http://.*?)</src>").getMatch(0);
            if (link == null) return decryptedLinks;

            DownloadLink dl = createDownloadlink("directhttp://" + link);
            dl.setFinalFileName(getVideoTitle(videoTitle));
            decryptedLinks.add(dl);
        }
        if (videoTitle != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(videoTitle.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    private String getVideoTitle(String s) {
        if (s == null) s = "UnknownTitle";
        s = s.replace("._Part", "_Part");
        String ext = br.getRegex("type=\"video/([0-9a-zA-Z]{3,5})\"").getMatch(0);
        ext = ext != null ? ext : "mp4";
        return s.trim() + "." + ext;
    }

}