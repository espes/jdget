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
import java.util.Random;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "justin.tv" }, urls = { "http://(((www\\.)?(justin\\.tv)|(www\\.|[a-z]{2}\\.)?(twitchtv\\.com|twitch\\.tv))/[^<>/\"]+/((b|c)/\\d+|videos(\\?page=\\d+)?)|(www\\.)?(justin|twitch)\\.tv/archive/archive_popout\\?id=\\d+)" }, flags = { 0 })
public class JustinTvDecrypt extends PluginForDecrypt {

    public JustinTvDecrypt(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String HOSTURL = "http://www.justindecrypted.tv";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        // twitchtv belongs to justin.tv
        br.setCookie("http://justin.tv", "fl", "en-us");
        // redirects occur to de.domain when browser accept language set to German!
        br.getHeaders().put("Accept-Language", "en-gb");
        // currently redirect to www.
        String parameter = param.toString().replaceAll("://([a-z]{2}\\.)?(twitchtv\\.com|twitch\\.tv)", "://www.twitch.tv");
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML(">Sorry, we couldn\\'t find that stream\\.|<h1>This channel is closed</h1>")) {
            // final Regex info = new Regex(parameter,
            // "(twitchtv\\.com|twitch\\.tv))/[^<>/\"]+/((b|c)/\\d+|videos(\\?page=\\d+)?)");
            final DownloadLink dlink = createDownloadlink("http://media" + new Random().nextInt(1000) + ".twitchdecrypted.tv/archives/" + new Regex(parameter, "(\\d+)$").getMatch(0) + ".flv");
            dlink.setAvailable(false);
            decryptedLinks.add(dlink);
            return decryptedLinks;
        }
        if (parameter.matches("http://(www\\.)?(justin|twitch)\\.tv/archive/archive_popout\\?id=\\d+")) {
            parameter = "http://www.justin.tv/" + System.currentTimeMillis() + "/b/" + new Regex(parameter, "(\\d+)$").getMatch(0);
        }
        if (parameter.contains("/videos")) {
            final String username = new Regex(parameter, "/([^<>\"/]*?)/videos").getMatch(0);
            if (br.containsHTML("<strong id=\"videos_count\">0")) {
                logger.info("Nothing to decrypt here: " + parameter);
                return decryptedLinks;
            }

            final String[] decryptAgainLinks = br.getRegex("(\\'|\")(/" + username + "/(b|c)/\\d+)(\\'|\")").getColumn(1);
            if (decryptAgainLinks == null || decryptAgainLinks.length == 0) {
                logger.warning("Decrypter broken: " + parameter);
                return null;
            }
            for (final String dl : decryptAgainLinks)
                decryptedLinks.add(createDownloadlink("http://twitch.tv" + dl));
        } else {
            if (br.getURL().contains("/videos")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String filename = null;
            String channelName = null;
            String date = null;
            if (parameter.contains("justin.tv/")) {
                filename = br.getRegex("<h2 class=\"clip_title\">([^<>\"]*?)</h2>").getMatch(0);
            } else {
                // Testlink: http://www.twitch.tv/fiegsy/b/296921448
                filename = br.getRegex("<span class='real_title js\\-title'>(.*?)</span>").getMatch(0);
                if (filename == null) filename = br.getRegex("<h2 class='js\\-title'>(.*?)</h2>").getMatch(0);
                channelName = br.getRegex("class=\"channelname\">([^<>\"]*?)</a>").getMatch(0);
                date = br.getRegex("<time datetime=\\'(\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)\\'></time>").getMatch(0);
                // they don't give full title with this regex, badddd
                // eg. http://www.twitch.tv/fgtvlive/c/2006335
                // returns: 'FgtvLive' from <meta property="og:title" content="FgtvLive"/>
                // filename = br.getRegex("<meta property=\"og:title\" content=\"([^<>\"]*?)\"").getMatch(0);
            }
            if (parameter.contains("/b/")) {
                br.getPage("http://api.justin.tv/api/broadcast/by_archive/" + new Regex(parameter, "(\\d+)$").getMatch(0) + ".xml");
                if (filename == null) filename = br.getRegex("<title>([^<>\"]*?)</title>").getMatch(0);
            } else {
                br.getPage("http://api.justin.tv/api/broadcast/by_chapter/" + new Regex(parameter, "(\\d+)$").getMatch(0) + ".xml");
            }
            final String[] links = br.getRegex("<video_file_url>(http://[^<>\"]*?)</video_file_url>").getColumn(0);
            if (links == null || links.length == 0 || filename == null) {
                logger.warning("Decrypter broken: " + parameter);
                return null;
            }
            filename = Encoding.htmlDecode(filename.trim());
            filename = filename.replaceAll("[\r\n#]+", "");
            int counter = 1;
            String format = String.format("%%0%dd", (int) Math.log10(links.length) + 1);
            final PluginForHost hostPlugin = JDUtilities.getPluginForHost("justin.tv");

            for (String dl : links) {
                final DownloadLink dlink = createDownloadlink(dl.replace("twitch.tv/", "twitchdecrypted.tv/").replace("justin.tv/", "justindecrypted.tv/"));
                dlink.setProperty("directlink", "true");
                dlink.setProperty("plainfilename", filename);
                dlink.setProperty("partnumber", counter);
                if (date != null) dlink.setProperty("originaldate", date);
                if (channelName != null) dlink.setProperty("channel", Encoding.htmlDecode(channelName.trim()));
                final String formattedFilename = ((jd.plugins.hoster.JustinTv) hostPlugin).getFormattedFilename(dlink);
                dlink.setName(formattedFilename);
                decryptedLinks.add(dlink);
                counter++;
            }
            final FilePackage fp = FilePackage.getInstance();
            fp.setName(filename);
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}