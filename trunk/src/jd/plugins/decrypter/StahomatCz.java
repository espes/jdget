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
import jd.controlling.AccountController;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision: 25510 $", interfaceVersion = 2, names = { "stahomat.cz" }, urls = { "http://(www\\.)?stahomat\\.(cz|sk)/detail/[^<>\"]*" }, flags = { 0 })
public class StahomatCz extends PluginForDecrypt {

    public StahomatCz(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void prepBrowser() {
        // define custom browser headers and language settings.
        br.getHeaders().put("Accept-Language", "en-gb, en;q=0.9");
        br.getHeaders().put("User-Agent", "JDownloader");
        br.setCustomCharset("utf-8");
        br.setConnectTimeout(3 * 60 * 1000);
        br.setReadTimeout(3 * 60 * 1000);
    }

    /**
     * JD2 CODE: DO NOIT USE OVERRIDE FÒR COMPATIBILITY REASONS!!!!!
     */
    public boolean isProxyRotationEnabledForLinkCrawler() {
        return false;
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        prepBrowser();
        final String parameter = param.toString();
        final PluginForHost hosterPlugin = JDUtilities.getPluginForHost("stahomat.cz");
        final Account aa = AccountController.getInstance().getValidAccount(hosterPlugin);
        if (aa == null) {
            logger.info("Can't decrypt links without account...");
            final DownloadLink offline = createDownloadlink("directhttp://" + parameter);
            offline.setAvailable(false);
            offline.setProperty("OFFLINE", true);
            decryptedLinks.add(offline);
            return decryptedLinks;
        }
        final String token = aa.getStringProperty("token", null);
        if (token == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.getPage("http://api.stahomat.cz/a-p-i/getoriginurl?token=" + token + "&url=" + Encoding.urlEncode(parameter));
        if (br.containsHTML("<title>404 - stahomat\\.cz</title>")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String finallink = getJson("origin_url");
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        finallink = finallink.replace("\\", "");
        decryptedLinks.add(createDownloadlink(finallink));

        return decryptedLinks;
    }

    private String getJson(final String key) {
        String result = br.getRegex("\"" + key + "\":\"([^\"]+)\"").getMatch(0);
        if (result == null) {
            result = br.getRegex("\"" + key + "\":([^\"\\}\\,]+)").getMatch(0);
        }
        return result;
    }

}
