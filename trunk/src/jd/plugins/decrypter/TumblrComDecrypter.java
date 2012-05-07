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
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "tumblr.com" }, urls = { "http://(www\\.)?(tumblr\\.com/audio_file/\\d+/tumblr_[A-Za-z0-9]+|[\\w\\.\\-]*?tumblr\\.com/post/\\d+)" }, flags = { 0 })
public class TumblrComDecrypter extends PluginForDecrypt {

    public TumblrComDecrypter(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        if (parameter.matches("http://(www\\.)?tumblr\\.com/audio_file/\\d+/tumblr_[A-Za-z0-9]+")) {
            br.setFollowRedirects(false);
            br.getPage(parameter);
            String finallink = br.getRedirectLocation();
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            decryptedLinks.add(createDownloadlink(finallink));
        } else {
            br.setFollowRedirects(true);
            br.getPage(parameter);
            final String fpName = br.getRegex("<title>([^<>]*?)</title>").getMatch(0);
            final String[] links = br.getRegex("<meta property=\"og:image\" content=\"(http://[^<>\"]*?)\"").getColumn(0);
            if (links != null && links.length > 1) {
                for (final String piclink : links) {
                    decryptedLinks.add(createDownloadlink("directhttp://" + piclink));
                }
                if (fpName != null) {
                    FilePackage fp = FilePackage.getInstance();
                    fp.setName(Encoding.htmlDecode(fpName.trim()));
                    fp.addLinks(decryptedLinks);
                }
            } else {
                // Single links go to the host plugin, could be videos/mp3s
                decryptedLinks.add(createDownloadlink(parameter.replace("tumblr.com/", "tumblrdecrypted.com/")));
            }
        }
        return decryptedLinks;
    }

}
