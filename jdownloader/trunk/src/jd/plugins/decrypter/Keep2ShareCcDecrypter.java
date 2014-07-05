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
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "keep2share.cc" }, urls = { "http://(www\\.)?(keep2share|k2s|k2share|keep2s|keep2)\\.cc/file/(info/)?[a-z0-9]+" }, flags = { 0 })
public class Keep2ShareCcDecrypter extends PluginForDecrypt {

    public Keep2ShareCcDecrypter(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        parameter = parameter.replace("keep2share.cc/", "k2s.cc/");
        final PluginForHost plugin = JDUtilities.getPluginForHost("keep2share.cc");
        if (plugin == null) {
            throw new IllegalStateException("keep2share plugin not found!");
        }
        // set cross browser support
        ((jd.plugins.hoster.Keep2ShareCc) plugin).setBrowser(br);
        ((jd.plugins.hoster.Keep2ShareCc) plugin).getPage(parameter);
        // Check if we have a single link or a folder
        if (br.containsHTML("class=\"summary\"")) {
            final String fpName = br.getRegex("<title>([^<>\"]*?)</title>").getMatch(0);
            final String[] links = br.getRegex("target=\"_blank\" href=\"([^\"]+)?(/file/[a-z0-9]+)").getColumn(1);
            if (links == null || links.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            for (final String link : links) {
                decryptedLinks.add(createDownloadlink("http://keep2sharedecrypted.cc" + link));
            }
            if (fpName != null) {
                final FilePackage fp = FilePackage.getInstance();
                fp.setName(Encoding.htmlDecode(fpName.trim()));
                fp.addLinks(decryptedLinks);
            }
        } else {
            final DownloadLink singlink = createDownloadlink("http://keep2sharedecrypted.cc/file/" + new Regex(parameter, "([a-z0-9]+)$").getMatch(0));
            final String filename = ((jd.plugins.hoster.Keep2ShareCc) plugin).getFileName();
            final String filesize = ((jd.plugins.hoster.Keep2ShareCc) plugin).getFileSize();
            if (filename != null) {
                singlink.setName(Encoding.htmlDecode(filename.trim()));
            }
            if (filesize != null) {
                singlink.setDownloadSize(SizeFormatter.getSize(filesize.trim()));
            }
            if (br.containsHTML("Downloading blocked due to")) {
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Downloading blocked: No JD bug, please contact the keep2share support", 10 * 60 * 1000l);
            }
            // you can set filename for offline links! handling should come here!
            if (br.containsHTML("Sorry, an error occurred while processing your request|File not found or deleted|>Sorry, this file is blocked or deleted\\.</h5>|class=\"empty\"|>Displaying 1")) {
                singlink.setAvailable(false);
            }
            if (filename == null) {
                singlink.setAvailable(false);
            } else {
                // prevent wasteful double linkchecks.
                singlink.setAvailable(true);
            }
            decryptedLinks.add(singlink);
        }

        return decryptedLinks;
    }

}
