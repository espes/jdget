//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 13393 $", interfaceVersion = 2, names = { "mirrorstack.com" }, urls = { "http://(www\\.)?(mirrorstack\\.com|uploading\\.to|copyload\\.com|multishared\\.com)/([a-z0-9]{2}_)?[a-z0-9]{12}" }, flags = { 0 })
public class MirStkCm extends PluginForDecrypt {

    /*
     * TODO many sites are using this type of script. Rename this plugin into
     * general/template type plugin naming scheme (find the name of the script
     * and rename). Do this after next major update, when we can delete plugins
     * again.
     */

    /*
     * DEV NOTES: (mirrorshack) - provider has issues at times, and doesn't
     * unhash stored data values before exporting them into redirects. I've
     * noticed this with mediafire links for example
     * http://mirrorstack.com/mf_dbfzhyf2hnxm will at times return
     * http://www.mediafire.com/?HASH(0x15053b48), you can then reload a couple
     * times and it will work in jd.. provider problem not plugin. Other example
     * links I've used seem to work fine. - Please keep code generic as
     * possible.
     * 
     * Don't use package name as these type of link protection services export a
     * list of hoster urls of a single file. When one imports many links
     * (parts), JD loads many instances of the decrypter and each
     * url/parameter/instance gets a separate packagename and that sucks. It's
     * best to use linkgrabbers default auto packagename sorting.
     */

    // version 0.5

    public MirStkCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        // easier to set redirects on and off than renaming parameter them all
        // and it also creates less maintenance if provider changes things up.
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML("(?i)>(File )?Not Found</")) {
            logger.warning("Invalid URL, either removed or never existed :" + parameter);
            return null;
        }
        br.setFollowRedirects(false);
        String finallink = null;
        String[] singleLinks = null;
        // Add a single link parameter to String[]
        if (parameter.matches("http://[^/<>\"\\' ]+/[a-z0-9]{2}_[a-z0-9]{12}")) {
            singleLinks = new Regex(parameter, "(.+)").getColumn(0);
        }
        // Standard parameter, find all singleLinks
        else if (parameter.matches("http://[^/<>\"\\' ]+/[a-z0-9]{12}")) {
            singleLinks = br.getRegex("<a href=\\'(http://[^/<>\"\\' ]+/[a-z0-9]{2}_[a-z0-9]{12})\\'").getColumn(0);
            if (singleLinks == null || singleLinks.length == 0) {
                logger.warning("Couldn't find singleLinks... :" + parameter);
                return null;
            }
        }
        // Process links found. Each provider has a slightly different
        // requirement and outcome
        progress.setRange(singleLinks.length);
        for (String singleLink : singleLinks) {
            if (parameter.contains("uploading.to/") || parameter.contains("multishared.com/")) {
                br.getHeaders().put("Referer", new Regex(parameter, "(https?://[\\w+\\.\\d\\-]+(:\\d+)?)/").getMatch(0) + "/r_counter");
                br.getPage(singleLink);
                finallink = br.getRegex("frame src=\"(https?://[^\"\\' <>]+)\"").getMatch(0);
            } else if (parameter.contains("copyload.com/")) {
                br.getHeaders().put("Referer", new Regex(parameter, "(https?://[\\w+\\.\\d\\-]+(:\\d+)?)/").getMatch(0) + "/r_counter");
                br.getPage(singleLink);
                finallink = br.getRedirectLocation();
            } else {
                br.getPage(singleLink);
                finallink = br.getRedirectLocation();
            }
            if (finallink == null) {
                logger.warning("WARNING: Couldn't find finallink. Please report this issue to JD Developement team. :" + parameter);
                logger.warning("Continuing...");
                continue;
            }
            decryptedLinks.add(createDownloadlink(finallink));
            progress.increase(1);
        }
        return decryptedLinks;
    }

}
