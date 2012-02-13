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
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "anime-stream24.com" }, urls = { "http://(www\\.)?anime-stream24\\.com/\\d+/\\d+/.*?\\.html" }, flags = { 0 })
public class NmStrm24Com extends PluginForDecrypt {

    public NmStrm24Com(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        ArrayList<String> cryptedLinks = new ArrayList<String>();
        ArrayList<String> regexes = new ArrayList<String>();
        regexes.add("scrolling=\"no\" src=\"(.*?)\"");
        regexes.add("<object data=\"(.*?)\"");
        regexes.add("style=\"display: none;\"><script src=\"(.*?)\"");
        regexes.add("flashvars=\\'file=(http://[^<>\"\\']+)\\&image");
        regexes.add("<iframe SRC=\"(http://[^<>\"\\']+)\"");
        regexes.add("<iframe SRC=\\'(http://[^<>\"\\']+)\\'");
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML("Seite nicht gefunden<")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String fpName = br.getRegex("\\'pageName\\': \\'([^<>\"\\']+)\\'").getMatch(0);
        if (fpName != null) fpName = br.getRegex("class=\\'post\\-title entry\\-title\\'>[\t\n\r ]+<a href=\\'http://[^<>\"\\']+\\'>([^<>\"\\']+)</a>").getMatch(0);
        String[] links = br.getRegex("id=\"fragment\\-\\d+\"><iframe (style=\\'overflow: hidden; border: 0; width: 600px; height: 480px\\' )?(src|SRC)=(\\'|\")(.*?)(\\'|\")").getColumn(3);
        if (links != null && links.length != 0) {
            for (String cryptedLink : links)
                cryptedLinks.add(cryptedLink);
        }
        for (String regex : regexes) {
            String tempLinks[] = br.getRegex(Pattern.compile(regex, Pattern.CASE_INSENSITIVE)).getColumn(0);
            if (tempLinks != null && tempLinks.length != 0) {
                for (String tempLink : tempLinks)
                    cryptedLinks.add(Encoding.htmlDecode(tempLink));
            }
        }
        final String dailyMotionUrl = br.getRegex("\"(http://dai\\.ly/[A-Za-z0-9]+)\"").getMatch(0);
        if (dailyMotionUrl != null) {
            br.getPage(dailyMotionUrl);
            final String finallink = br.getRegex("\"stream_h264_url\":\"(http:[^<>\"\\']+)\"").getMatch(0);
            if (finallink != null) decryptedLinks.add(createDownloadlink("directhttp://" + finallink.replace("\\", "")));
        }
        if (cryptedLinks == null || cryptedLinks.size() == 0) return null;
        for (String aLink : cryptedLinks)
            decryptedLinks.add(createDownloadlink(aLink));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpName.trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
