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
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filestube.com" }, urls = { "http://(www\\.)?(filestube\\.com/(?!source|search|look_for\\.html.+|sponsored_go.html.+)|video\\.filestube\\.com/watch,[a-z0-9]+/).+\\.html" }, flags = { 0 })
public class FlStbCm extends PluginForDecrypt {

    public FlStbCm(PluginWrapper wrapper) {
        super(wrapper);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        FilePackage fp = FilePackage.getInstance();
        final String parameter = param.toString();
        // Allows us to get age restricted videos
        br.setCookie("http://filestube.com/", "adultChecked", "1");
        br.getPage(parameter);
        if (parameter.contains("/go.html")) {
            String finallink = br.getRegex("<noframes> <br /> <a href=\"(.*?)\"").getMatch(0);
            if (finallink == null) finallink = br.getRegex("<iframe style=\".*?\" src=\"(.*?)\"").getMatch(0);
            if (finallink == null) return null;
            decryptedLinks.add(createDownloadlink(finallink));
        } else if (parameter.contains("video.filestube.com/watch")) {
            if (br.containsHTML("(>Error 404 video not found<|>Sorry, the video you requested does not exist)")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String externID = br.getRegex("name=\"src\" value=\"http://(www\\.)?youtube\\.com/v/([^<>\"\\'/\\&]+)(\\&|\")").getMatch(1);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://www.youtube.com/watch?v=" + externID));
                return decryptedLinks;
            }
            externID = br.getRegex("dailymotion\\.com/swf/video/([a-z0-9\\-_]+)\"").getMatch(0);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://www.dailymotion.com/video/" + externID + "_" + System.currentTimeMillis()));
                return decryptedLinks;
            }
            externID = br.getRegex("xvideos\\.com/embedframe/(\\d+)\"").getMatch(0);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://www.xvideos.com/video" + externID));
                return decryptedLinks;
            }
            externID = br.getRegex("metacafe\\.com/fplayer/(\\d+)/").getMatch(0);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://www.metacafe.com/watch/" + externID + "/" + System.currentTimeMillis()));
                return decryptedLinks;
            }
            externID = br.getRegex("emb\\.slutload\\.com/([A-Za-z0-9]+)\"").getMatch(0);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://slutload.com/watch/" + externID));
                return decryptedLinks;
            }
            externID = br.getRegex("123video\\.nl/123video_emb\\.swf\\?mediaSrc=(\\d+)\"").getMatch(0);
            if (externID != null) {
                decryptedLinks.add(createDownloadlink("http://www.123video.nl/playvideos.asp?MovieID=" + externID));
                return decryptedLinks;
            }
            // Filename needed for all ids below here
            String filename = br.getRegex("<meta property=\"og:title\" content=\"([^<>\"]*?)\"").getMatch(0);
            if (filename == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            filename = Encoding.htmlDecode(filename.trim());
            externID = br.getRegex("myspace\\.com/videos/vplayer\\.swf\" /><param name=\"flashvars\" value=\"m=(\\d+)").getMatch(0);
            if (externID != null) {
                br.getPage("http://mediaservices.myspace.com/services/rss.ashx?videoID=" + externID + "&type=video&el=");
                final String finallink = br.getRegex("<media:player url=\"(http://[^<>\"]*?)\"").getMatch(0);
                if (finallink == null) {
                    logger.warning("Decrypter broken for link: " + parameter);
                    return null;
                }
                final DownloadLink dl = createDownloadlink("directhttp://" + finallink);
                dl.setFinalFileName(filename + ".flv");
                decryptedLinks.add(dl);
                return decryptedLinks;
            }
            externID = br.getRegex("shufuni\\.com/Flash/.*?flashvars=\"VideoCode=(.*?)\"").getMatch(0);
            if (externID != null) {
                DownloadLink dl = createDownloadlink("http://www.shufuni.com/handlers/FLVStreamingv2.ashx?videoCode=" + externID);
                dl.setFinalFileName(Encoding.htmlDecode(filename.trim()));
                decryptedLinks.add(dl);
                return decryptedLinks;
            }
            externID = br.getRegex("ebaumsworld\\.com/player\\.swf\" allowScriptAccess=\"always\" flashvars=\"id1=(\\d+)\"").getMatch(0);
            if (externID != null) {
                br.getPage("http://www.ebaumsworld.com/video/player/" + externID + "?env=id1");
                externID = br.getRegex("<file>(http://[^<>\"]*?)</file>").getMatch(0);
                if (externID != null) {
                    DownloadLink dl = createDownloadlink("directhttp://" + externID);
                    dl.setFinalFileName(Encoding.htmlDecode(filename.trim()) + ".flv");
                    decryptedLinks.add(dl);
                    return decryptedLinks;
                }
            }
            if (externID == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
        } else {
            if (br.containsHTML("(> File no longer available|>Error 404 \\- Requested file was not found<)")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String fpName = br.getRegex("<title>(.*?)\\- Download").getMatch(0);
            // Hmm this plugin should always have a name with that mass of
            // alternative ways to get the name
            if (fpName == null) {
                fpName = br.getRegex("content=\"Download(.*?)from").getMatch(0);
                if (fpName == null) {
                    fpName = br.getRegex("\">Download:(.*?)</h2>").getMatch(0);
                    if (fpName == null) {
                        fpName = br.getRegex("widgetTitle: \\'(.*?)\\',").getMatch(0);
                        if (fpName == null) {
                            fpName = br.getRegex("&quot;\\](.*?)\\[/url\\]\"").getMatch(0);
                        }
                    }
                }
            }
            String pagePiece = br.getRegex(Pattern.compile("id=\"copy_paste_links\" style=\".*?\">(.*?)</pre>", Pattern.DOTALL)).getMatch(0);
            // Find IDs for alternative links
            String[][] alternativeLinks = br.getRegex("alternate_files\\.push\\(\\{key: \\'([a-z0-9]+)\\',token: \\'([a-z0-9]+)\\'\\}\\)").getMatches();
            if (pagePiece == null) return null;
            String temp[] = pagePiece.split("\r\n");
            if (temp == null) return null;
            if (temp == null || temp.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            for (String data : temp)
                decryptedLinks.add(createDownloadlink(data));
            if (alternativeLinks != null && alternativeLinks.length != 0) {
                Browser br2 = br.cloneBrowser();
                progress.setRange(alternativeLinks.length);
                for (String alternativeLinkInfo[] : alternativeLinks) {
                    br2.getPage("http://149.13.65.144:8889/get/" + alternativeLinkInfo[0] + "/" + alternativeLinkInfo[1] + "?callback=jsonp" + System.currentTimeMillis());
                    String alts[] = br2.getRegex("\\'t\\':\\'(.*?)\\'").getColumn(0);
                    if (alts != null && alts.length != 0) {
                        Browser br3 = br.cloneBrowser();
                        for (String link : alts) {
                            br3.getPage("http://www.filestube.com/" + link + "/go.html");
                            String finallink = br3.getRegex("<noframes> <br /> <a href=\"(.*?)\"").getMatch(0);
                            if (finallink == null) finallink = br3.getRegex("<iframe style=\".*?\" src=\"(.*?)\"").getMatch(0);
                            if (finallink != null) decryptedLinks.add(createDownloadlink(finallink));
                        }
                        // 30 links = increase it by one
                        progress.increase(1);
                    }
                }
            }

            if (fpName != null) {
                fp.setName(fpName.trim());
                fp.addLinks(decryptedLinks);
            }
        }
        return decryptedLinks;
    }
}
