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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "multiupload.com" }, urls = { "http://(www\\.)?multiupload\\.[a-z]{2,3}/([A-Z0-9]{2}_[A-Z0-9]+|[0-9A-Z]+)" }, flags = { 0 })
public class MltpldCm extends PluginForDecrypt {

    public MltpldCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        br.setFollowRedirects(false);
        String parameter = param.toString();
        br.getPage(parameter);
        if (br.getRedirectLocation() != null) {
            /* domain redirection */
            br.getPage(br.getRedirectLocation());
        }
        if (br.containsHTML(">Unfortunately, the link you have clicked is not available")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        if (br.containsHTML(">UNKNOWN ERROR<")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unknownerror", "Unknown error caused by multiupload.com."));
        /** Check if there are links for the hosterplugin */
        if (br.containsHTML("(<div[^>]+id=\"downloadbutton_\"|document\\.getElementById\\(\\'dlbutton\\'\\))")) {
            String domain = new Regex(parameter, "http.*?//.*?(multiupload\\..*?)/").getMatch(0);
            if (domain == null) {
                domain = "multiupload.com";
            }
            decryptedLinks.add(createDownloadlink(parameter.replace(domain, "multiuploaddecrypted.com/")));
        }
        if (!parameter.contains("_")) {
            String[] redirectLinks = br.getRegex(Pattern.compile("id=\"url_\\d+\"><a href=\"(http://(www\\.)?multiupload\\.[a-z]{2,3}/.*?)\"")).getColumn(0);
            if (redirectLinks == null || redirectLinks.length == 0) {
                logger.warning("redirectLinks list is null...");
                return null;
            }
            progress.setRange(redirectLinks.length);
            for (String redirectLink : redirectLinks) {
                br.getPage(redirectLink);
                String finallink = br.getRedirectLocation();
                if (finallink == null) {
                    continue;
                }
                if (finallink.contains("mediafire")) finallink = finallink.replace("mediafire.com?", "mediafire.com/?");
                decryptedLinks.add(createDownloadlink(finallink));
                progress.increase(1);
            }
        } else {
            String finallink = br.getRedirectLocation();
            if (finallink == null) return null;
            decryptedLinks.add(createDownloadlink(finallink));
        }

        return decryptedLinks;
    }
}
