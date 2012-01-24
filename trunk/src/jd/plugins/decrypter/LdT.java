//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

import java.io.File;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.RandomUserAgent;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.plugins.hoster.DirectHTTP;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "iload.to" }, urls = { "http://((beta|de)\\.)?iload\\.to/(de/)?((go/\\d+([-\\w/\\.]+)?(/merged|go/\\d+)?)(streaming/.+)?|(view|title|release)/.*?/)" }, flags = { 0 })
public class LdT extends PluginForDecrypt {

    private final String patternSupported_Info = ".*?((beta|de)\\.)?iload\\.to/(de/)?(view|title|release)/.*?/";

    public LdT(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(final CryptedLink param, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final ArrayList<String> alllinks = new ArrayList<String>();
        final String parameter = param.toString();
        setBrowserExclusive();
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        if (parameter.matches(patternSupported_Info)) {
            br.getPage(parameter);
            if (br.getRedirectLocation() != null) {
                br.getPage(br.getRedirectLocation());
            }
            final String hosterlinks[] = br.getRegex("href=\"((/de)?/go/\\d+-.*?/)\"").getColumn(0);
            final String streamlinks[] = br.getRegex("\"((/de)?/go/\\d+-[a-z0-9\\.-]+/streaming/.*?)\"").getColumn(0);
            if ((hosterlinks == null || hosterlinks.length == 0) && (streamlinks == null || streamlinks.length == 0)) { return null; }
            if (hosterlinks != null && hosterlinks.length != 0) {
                logger.info("Found " + hosterlinks.length + " hosterlinks, decrypting now...");
                for (final String hosterlink : hosterlinks) {
                    if (!hosterlink.contains("/streaming/")) {
                        alllinks.add(hosterlink);
                    }
                }
            }
            if (streamlinks != null && streamlinks.length != 0) {
                logger.info("Found " + streamlinks.length + " streamlinks, decrypting now...");
                for (final String streamlink : streamlinks) {
                    alllinks.add(streamlink);
                }
            }
            logger.info("Found links to " + alllinks.size() + ". Decrypting now...");
            progress.setRange(alllinks.size());
            for (final String link : alllinks) {
                final String golink = "http://iload.to/" + link;
                br.getPage(golink);
                final String finallink = br.getRedirectLocation();
                if (finallink == null) { return null; }
                final DownloadLink dl_link = createDownloadlink(finallink);
                dl_link.addSourcePluginPassword("iload.to");
                decryptedLinks.add(dl_link);
                progress.increase(1);
            }
        } else {
            br.getPage(parameter);
            if (br.getRedirectLocation() == null) {
                if (br.containsHTML("Deine Anfragen sehen aus als ob sie von einem Bot kommen")) {
                    for (int i = 0; i < 5; i++) {
                        if (br.containsHTML("recaptcha/api/")) {
                            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
                            rc.parse();
                            rc.load();
                            final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                            final String c = getCaptchaCode(cf, param);
                            rc.setCode(c);
                        }
                        if (br.containsHTML("recaptcha/api/")) {
                            continue;
                        } else {
                            break;
                        }
                    }
                } else {
                    return null;
                }
            }
            if (br.getRedirectLocation().equalsIgnoreCase(parameter) || br.getRedirectLocation().equalsIgnoreCase(parameter + "/")) {
                br.getPage(parameter);
            }
            if (br.getRedirectLocation().equalsIgnoreCase(parameter)) { return null; }
            final String url = br.getRedirectLocation();
            DownloadLink dl;
            decryptedLinks.add(dl = createDownloadlink(url));
            dl.addSourcePluginPassword("iload.to");
            dl.setUrlDownload(url);
        }
        return decryptedLinks;
    }

}
