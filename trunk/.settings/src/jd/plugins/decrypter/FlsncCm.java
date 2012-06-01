package jd.plugins.decrypter;

import java.util.ArrayList;
import java.util.HashMap;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 16251 $", interfaceVersion = 2, names = { "filesonic.com" }, urls = { "http://[\\w\\.]*?filesonic\\..*?/.*?folder/[0-9a-z]+" }, flags = { 0 })
public class FlsncCm extends PluginForDecrypt {

    private static String geoDomain = null;

    public FlsncCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    private synchronized String getDomain() {
        if (geoDomain != null) return geoDomain;
        String defaultDomain = "http://www.filesonic.com";
        try {
            geoDomain = getDomainAPI();
            if (geoDomain == null) {
                Browser br = new Browser();
                br.setCookie(defaultDomain, "lang", "en");
                br.setFollowRedirects(false);
                br.getPage(defaultDomain);
                geoDomain = br.getRedirectLocation();
                if (geoDomain == null) {
                    geoDomain = defaultDomain;
                } else {
                    String domain = new Regex(br.getRedirectLocation(), "http://.*?(filesonic\\..*?)/").getMatch(0);
                    geoDomain = "http://www." + domain;
                }
            }
        } catch (final Throwable e) {
            geoDomain = defaultDomain;
        }
        return geoDomain;
    }

    private synchronized String getDomainAPI() {
        try {
            Browser br = new Browser();
            br.setFollowRedirects(true);
            br.getPage("http://api.filesonic.com/utility?method=getFilesonicDomainForCurrentIp");
            String domain = br.getRegex("response>.*?filesonic(\\..*?)</resp").getMatch(0);
            if (domain != null) { return "http://www.filesonic" + domain; }
        } catch (final Throwable e) {
            logger.severe(e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        String id = new Regex(parameter, "/(folder/[0-9a-z]+)").getMatch(0);
        if (id == null) return null;
        parameter = getDomain() + "/" + id;
        boolean failed = false;
        br.getPage(parameter);
        if (br.getRedirectLocation() != null) br.getPage(br.getRedirectLocation());
        if (br.containsHTML(">No links to show<")) {
            logger.info("Folder empty: " + parameter);
            return decryptedLinks;
        }
        if (br.containsHTML("(>The page you are trying to access was not found|>Error 404 \\- Page Not Found<)")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String[] links = br.getRegex("\"(" + getDomain() + "/file/[^\"]+)\"").getColumn(0);
        if (links == null || links.length == 0) {
            failed = true;
            links = br.getRegex("<td><a href=\"(http://.*?)\"").getColumn(0);
            if (links == null || links.length == 0) links = br.getRegex("\"(http://[^/\" ]*?filesonic\\..*?/[^\" ]*?file/[a-zA-Z0-9]+/.*?)\"").getColumn(0);
        }
        String[][] folderLinks = br.getRegex("\"(http://(www\\.)?filesonic\\..{2,3}/folder/\\d+)\">(.*?)</a> \\(folder\\)</td>").getMatches();
        if ((links == null || links.length == 0) && (folderLinks == null || folderLinks.length == 0)) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        if (links != null && links.length != 0) {
            for (String data : links) {
                if (failed) {
                    if (!data.contains("/folder/")) decryptedLinks.add(createDownloadlink(data));
                } else {
                    String filename = new Regex(data, "filesonic\\..*?/.*?file/.*?/(.*?)\"").getMatch(0);
                    DownloadLink aLink = createDownloadlink(data);
                    if (filename != null) aLink.setName(filename.trim());
                    if (filename != null) aLink.setAvailable(true);
                    if (!data.contains("/folder/")) decryptedLinks.add(createDownloadlink(data));
                }
            }
        }
        HashMap<String, FilePackage> packageMap = new HashMap<String, FilePackage>();
        if (folderLinks != null && folderLinks.length != 0) {
            for (String[] folderLink : folderLinks) {
                DownloadLink lol = createDownloadlink(folderLink[0]);
                if (folderLink[2] != null) {
                    FilePackage fp = packageMap.get(folderLink[2].trim());
                    if (fp == null) {
                        fp = FilePackage.getInstance();
                        fp.setName(folderLink[2].trim());
                        packageMap.put(folderLink[2].trim(), fp);
                    }
                    fp.add(lol);
                }
                decryptedLinks.add(lol);
            }
        }
        return decryptedLinks;
    }

}
