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
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "sourceforge.net" }, urls = { "https?://(www\\.)?(sourceforge\\.net/projects/([^/]+/files/.+/download|[^/]+/)|downloads\\.sourceforge\\.net/.+)" }, flags = { 0 })
public class SourceForgeNet extends PluginForDecrypt {

    public SourceForgeNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("https://", "http://");
        // We get downloadlinks depending on our useragent
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        br.setFollowRedirects(true);
        // Test if we already have a direct link here
        URLConnectionAdapter con = br.openGetConnection(parameter);
        if (con.getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("(Error 404|The page you were looking for cannot be found|could not be found or is not available)")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            final String altDlink = br.getRegex("<b>Download</b>[\t\n\r ]+<small title=\"(/[^<>\"]*?)\"").getMatch(0);
            String link = null;
            if (parameter.contains("/files/extras/") || parameter.contains("prdownloads.sourceforge.net") || parameter.contains("/download")) {
                link = br.getRegex("Please use this([\n\t\r ]+)?<a href=\"(.*?)\"").getMatch(1);
                if (link == null) link = br.getRegex("\"(http://downloads\\.sourceforge\\.net/project/.*?/extras/.*?/.*?use_mirror=.*?)\"").getMatch(0);
            } else {
                final String project = new Regex(parameter, "sourceforge\\.net/projects/(.*?)/").getMatch(0);
                if (altDlink != null) {
                    // Avoid ad-installers, see here: http://userscripts.org/scripts/show/174951
                    link = "http://master.dl.sourceforge.net/project/" + project + altDlink;
                } else {
                    final String continuelink = br.getRegex("\"(/projects/" + project + "/files/latest/download[^<>\"/]*?)\"").getMatch(0);
                    if (continuelink == null) {
                        logger.info("Found no downloadable link for: " + parameter);
                        return decryptedLinks;
                    }
                    br.getPage(continuelink);
                    if (br.containsHTML("(<h1>Error encountered</h1>|>We apologize\\. It appears an error has occurred\\.)")) {
                        logger.info("Servererror for link: " + parameter);
                        throw new DecrypterException(JDL.L("plugins.decrypt.sourceforgenet.errormsg.servererror", "A server error happened, please try again or check in browser!"));
                    }
                    link = new Regex(Encoding.htmlDecode(br.toString()), "Please use this([\t\n\r ]+)?<a href=\"(http://.*?)\"").getMatch(1);
                }
            }
            if (link == null) {
                logger.warning("Decrypter broken, link: " + parameter);
                return null;
            }
            link = Encoding.htmlDecode(link);
            boolean getPage = false;
            final String urlPart = new Regex(link, "(http://downloads\\.sourceforge\\.net/project/.*?)(http://sourceforge\\.net/|\\?r=)").getMatch(0);
            final String secondUrlPart = new Regex(link, "(\\&ts=\\d+\\&use_mirror=.+)").getMatch(0);
            // Either we already got the final link or we have to build it
            if (urlPart != null && secondUrlPart != null) {
                link = urlPart + "?r=" + secondUrlPart;
                getPage = true;
            }
            br.setFollowRedirects(false);
            String finallink = null;
            boolean failed = true;
            for (int i = 0; i <= 5; i++) {
                if (getPage) {
                    br.getPage(link);
                    finallink = br.getRedirectLocation();
                    if (finallink == null) return null;
                } else {
                    finallink = link;
                }
                con = br.openGetConnection(finallink);
                if (con.getContentType().contains("html")) {
                    logger.info("finallink is no file, continuing...");
                    continue;
                }
                failed = false;
                break;
            }
            if (failed) logger.warning("The finallink is no file!!");
            decryptedLinks.add(createDownloadlink("directhttp://" + finallink));
            con.disconnect();
        } else {
            con.disconnect();
            decryptedLinks.add(createDownloadlink("directhttp://" + parameter));
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}