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

package jd.plugins.hoster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jd.PluginWrapper;
import jd.config.Property;
import jd.crypt.Base64;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.BrowserAdapter;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "uploaded.to" }, urls = { "(http://[\\w\\.-]*?uploaded\\.to/.*?(file/|\\?id=|&id=)[\\w]+/?)|(http://[\\w\\.]*?ul\\.to/(?!folder)(\\?id=|&id=)?[\\w\\-]+/.+)|(http://[\\w\\.]*?ul\\.to/(?!folder)(\\?id=|&id=)?[\\w\\-]+/?)" }, flags = { 2 })
public class Uploadedto extends PluginForHost {

    private static AtomicInteger maxPrem = new AtomicInteger(1);

    static class Sec {
        public static String d(final byte[] b, final byte[] key) {
            Cipher cipher;
            try {
                final IvParameterSpec ivSpec = new IvParameterSpec(key);
                final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                return new String(cipher.doFinal(b), "UTF-8");
            } catch (final Exception e) {
                e.printStackTrace();
                final IvParameterSpec ivSpec = new IvParameterSpec(key);
                final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

                try {
                    cipher = Cipher.getInstance("AES/CBC/nopadding");

                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
                    return new String(cipher.doFinal(b), "UTF-8");
                } catch (final Exception e1) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        private byte[] key;

        private byte[] prep;

        public Sec() {
            key = new byte[] { 0x01, 0x02, 0x11, 0x01, 0x01, 0x54, 0x01, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x22, 0x01 };
            prep = Base64.decode("MC8O21gQXUaeSgMxxiOGugSrROkQHTbadlwDeJqHOpU4Q2o38bGWkm3/2zfS0N0s");
        }

        public String run() {

            return new String(new byte[] { 97, 112, 105, 107, 101, 121 }) + "=" + d(prep, key);

        }
    }

    private static void workAroundTimeOut(final Browser br) {
        try {
            if (br != null) {
                br.setConnectTimeout(45000);
                br.setReadTimeout(45000);
            }
        } catch (final Throwable e) {
        }
    }

    public Uploadedto(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://uploaded.to/");
        this.setStartIntervall(2000l);
    }

    @Override
    public boolean checkLinks(DownloadLink[] urls) {
        if (urls == null || urls.length == 0) { return false; }
        try {
            Browser br = new Browser();
            workAroundTimeOut(br);
            br.setCookiesExclusive(true);
            StringBuilder sb = new StringBuilder();
            ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
            int index = 0;
            while (true) {
                links.clear();
                while (true) {
                    /* we test 80 links at once */
                    if (index == urls.length || links.size() > 80) break;
                    links.add(urls[index]);
                    index++;
                }
                sb.delete(0, sb.capacity());
                sb.append(new Sec().run());
                int c = 0;
                for (DownloadLink dl : links) {
                    sb.append("&id_" + c + "=" + getID(dl));
                    c++;
                }
                int retry = 0;
                while (true) {
                    /*
                     * workaround for api issues, retry 5 times when content
                     * length is only 20 bytes
                     */
                    if (retry == 5) return false;
                    br.postPage("http://uploaded.to/api/filemultiple", sb.toString());
                    if (br.getHttpConnection().getLongContentLength() != 20) {
                        break;
                    }
                    Thread.sleep(500);
                    retry++;
                }
                sb = null;
                String infos[][] = br.getRegex(Pattern.compile("(.*?),(.*?),(.*?),(.*?),(.*?)(\r|\n|$)")).getMatches();
                for (DownloadLink dl : links) {
                    String id = getID(dl);
                    int hit = -1;
                    for (int i = 0; i < infos.length; i++) {
                        if (infos[i][1].equalsIgnoreCase(id)) {
                            hit = i;
                            break;
                        }
                    }
                    if (hit == -1) {
                        /* id not in response, so its offline */
                        dl.setAvailable(false);
                    } else {
                        dl.setFinalFileName(infos[hit][4].trim());
                        dl.setDownloadSize(SizeFormatter.getSize(infos[hit][2]));
                        if ("online".equalsIgnoreCase(infos[hit][0].trim())) {
                            dl.setAvailable(true);
                            String sha1 = infos[hit][3].trim();
                            if (sha1.length() == 0) sha1 = null;
                            dl.setSha1Hash(sha1);
                            dl.setMD5Hash(null);
                        } else {
                            dl.setAvailable(false);
                        }
                    }
                }
                if (index == urls.length) break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void correctDownloadLink(DownloadLink link) {
        String url = link.getDownloadURL();
        url = url.replaceFirst("http://.*?/", "http://uploaded.to/");
        url = url.replaceFirst("\\.to/.*?id=", ".to/file/");
        if (!url.contains("/file/")) {
            url = url.replaceFirst("uploaded.to/", "uploaded.to/file/");
        }
        String[] parts = url.split("\\/");
        String newLink = "";
        for (int t = 0; t < Math.min(parts.length, 5); t++) {
            newLink += parts[t] + "/";
        }
        link.setUrlDownload(newLink);
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        /* reset maxPrem workaround on every fetchaccount info */
        maxPrem.set(1);
        br.postPage("http://uploaded.to/status", "uid=" + Encoding.urlEncode(account.getUser()) + "&upw=" + Encoding.urlEncode(account.getPass()));
        if (br.containsHTML("blocked")) {
            ai.setStatus("Too many failed logins! Wait 15 mins");
            account.setTempDisabled(true);
            return ai;
        }
        if (br.containsHTML("wrong password")) {
            ai.setStatus("Wrong password");
            account.setValid(false);
            return ai;
        }
        if (br.containsHTML("wrong user")) {
            ai.setStatus("Wrong username");
            account.setValid(false);
            return ai;
        }
        String isPremium = br.getMatch("status: (premium)");
        if (isPremium == null) {
            ai.setStatus("Registered (free) User");
            ai.setUnlimitedTraffic();
            try {
                maxPrem.set(1);
                account.setMaxSimultanDownloads(1);
            } catch (final Throwable e) {
            }
            account.setProperty("free", "true");
        } else {
            String traffic = br.getMatch("traffic: (\\d+)");
            String expire = br.getMatch("expire: (\\d+)");
            if (expire != null) ai.setValidUntil(Long.parseLong(expire) * 1000);
            ai.setStatus("Premium account");
            account.setValid(true);
            long max = 100 * 1024 * 1024 * 1024l;
            long current = Long.parseLong(traffic);
            ai.setTrafficMax(Math.max(max, current));
            ai.setTrafficLeft(current);
            try {
                maxPrem.set(-1);
                account.setMaxSimultanDownloads(-1);
            } catch (final Throwable e) {
            }
            account.setProperty("free", Property.NULL);
        }
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://uploaded.to/legal";
    }

    private String getID(DownloadLink downloadLink) {
        return new Regex(downloadLink.getDownloadURL(), "uploaded.to/file/(.*?)/").getMatch(0);
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    private String getPassword(DownloadLink downloadLink) throws Exception {
        String passCode = null;
        if (br.containsHTML("<h2>Authentifizierung</h2>")) {
            logger.info("pw protected link");
            if (downloadLink.getStringProperty("pass", null) == null) {
                passCode = getUserInput(null, downloadLink);
            } else {
                /* gespeicherten PassCode holen */
                passCode = downloadLink.getStringProperty("pass", null);
            }
        }
        return passCode;
    }

    @Override
    public int getTimegapBetweenConnections() {
        return 2000;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    public void doFree(DownloadLink downloadLink) throws Exception {
        workAroundTimeOut(br);
        String id = getID(downloadLink);
        br.setFollowRedirects(false);
        br.setCookie("http://uploaded.to/", "lang", "de");
        br.getPage("http://uploaded.to/language/de");
        br.getPage("http://uploaded.to/file/" + id);
        if (br.getRedirectLocation() != null && br.getRedirectLocation().contains(".to/404")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        if (br.getRedirectLocation() != null) br.getPage(br.getRedirectLocation());
        String passCode = null;
        if (br.containsHTML("<h2>Authentifizierung</h2>")) {
            passCode = getPassword(downloadLink);
            Form form = br.getForm(0);
            form.put("pw", Encoding.urlEncode(passCode));
            br.submitForm(form);
            if (br.containsHTML("<h2>Authentifizierung</h2>")) {
                downloadLink.setProperty("pass", null);
                throw new PluginException(LinkStatus.ERROR_RETRY, "Password wrong!");
            }
            downloadLink.setProperty("pass", passCode);
        }
        Browser brc = br.cloneBrowser();
        brc.getPage("http://uploaded.to/js/download.js");
        String recaptcha = brc.getRegex("Recaptcha\\.create\\(\"(.*?)\"").getMatch(0);
        if (recaptcha == null) {
            logger.severe(brc.toString());
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
        jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
        rc.setId(recaptcha.trim());
        rc.load();
        Form rcForm = new Form();
        rcForm.setMethod(MethodType.POST);
        rcForm.setAction("http://uploaded.to/io/ticket/captcha/" + getID(downloadLink));
        File cf = rc.downloadCaptcha(getLocalCaptchaFile());
        rc.setForm(rcForm);
        String wait = br.getRegex("Aktuelle Wartezeit: <span>(\\d+)</span> Sekunden</span>").getMatch(0);
        String c = getCaptchaCode(cf, downloadLink);
        rc.setCode(c);
        if (br.containsHTML("No connection to database")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
        if (br.containsHTML("err:\"captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        if (br.containsHTML("limit-dl")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 60 * 60 * 1000l);
        if (br.containsHTML("limit-parallel")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "You're already downloading", 60 * 60 * 1000l);
        if (br.containsHTML("limit-size")) throw new PluginException(LinkStatus.ERROR_FATAL, "Only Premiumusers are allowed to download files lager than 1,00 GB.");
        if (wait != null) {
            this.sleep(Integer.parseInt(wait) * 1000l, downloadLink);
        }
        String url = br.getRegex("url:'(http:.*?)'").getMatch(0);
        if (url == null) url = br.getRegex("url:'(dl/.*?)'").getMatch(0);
        if (url == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = BrowserAdapter.openDownload(br, downloadLink, url, false, 1);
        try {
            /* remove next major update */
            /* workaround for broken timeout in 0.9xx public */
            dl.getRequest().setConnectTimeout(30000);
            dl.getRequest().setReadTimeout(60000);
        } catch (final Throwable ee) {
        }
        if (!dl.getConnection().isContentDisposition()) {
            try {
                br.followConnection();
            } catch (final Throwable e) {
                logger.severe(e.getMessage());
            }
            if (dl.getConnection().getResponseCode() == 404) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (dl.getConnection().getResponseCode() == 508) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError(508)", 30 * 60 * 1000l);
            if (br.containsHTML("All of our free-download capacities are")) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "All of our free-download capacities are exhausted currently", 10 * 60 * 1000l);
            if (br.containsHTML("File not found!")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            if (br.containsHTML("No connection to database")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 10 * 60 * 1000l);
            if (br.getURL().contains("view=error")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 10 * 60 * 1000l);
            if (br.containsHTML("Aus technischen Gr") && br.containsHTML("ist ein Download momentan nicht m")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
            if ("No htmlCode read".equalsIgnoreCase(br.toString())) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
            if (br.containsHTML("Datei herunterladen")) {
                /*
                 * we get fresh entry page after clicking download, means we
                 * have to start from beginning
                 */
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Serverproblem", 5 * 60 * 1000l);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dl.getConnection().getResponseCode() == 404) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        dl.startDownload();
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        requestFileInformation(downloadLink);
        login(account);
        if (account.getProperty("free") != null) {
            br.getPage(downloadLink.getDownloadURL());
            doFree(downloadLink);
        } else {
            br.setFollowRedirects(false);
            String id = getID(downloadLink);
            br.getPage("http://uploaded.to/file/" + id + "/ddl");
            String error = new Regex(br.getRedirectLocation(), "http://uploaded.to/\\?view=(.*)").getMatch(0);
            if (error == null) {
                error = new Regex(br.getRedirectLocation(), "\\?view=(.*?)&i").getMatch(0);
            }
            if (error != null) {
                if (error.contains("error_traffic")) throw new PluginException(LinkStatus.ERROR_PREMIUM, JDL.L("plugins.hoster.uploadedto.errorso.premiumtrafficreached", "Traffic limit reached"), PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (br.getRedirectLocation() == null) {
                /* ul does not take care of set language.... */
                if (br.containsHTML(">Traffic exhausted") || br.containsHTML(">Traffickontingent aufgebraucht")) {
                    logger.info("Traffic exhausted, temp disabled account");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                }
                logger.info("InDirect Downloads active");
                Form form = br.getForm(0);
                if (form == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                if (form.getAction() != null && form.getAction().contains("register")) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                if (form.getAction() == null || form.getAction().contains("access")) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
                logger.info("Download from:" + form.getAction());
                dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, form, true, 0);
                dl.setFileSizeVerified(true);
            } else {
                logger.info("Direct Downloads active");
                logger.info("Download from:" + br.getRedirectLocation());
                dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, br.getRedirectLocation(), true, 0);
                dl.setFileSizeVerified(true);
            }
            try {
                /* remove next major update */
                /* workaround for broken timeout in 0.9xx public */
                dl.getRequest().setConnectTimeout(30000);
                dl.getRequest().setReadTimeout(60000);
            } catch (final Throwable ee) {
            }

            if (dl.getConnection().getLongContentLength() == 0 || !dl.getConnection().isContentDisposition()) {
                try {
                    br.followConnection();
                } catch (final Throwable e) {
                    logger.severe(e.getMessage());
                }
                if (dl.getConnection().getResponseCode() == 404) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
                if (dl.getConnection().getResponseCode() == 508) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError(508)", 30 * 60 * 1000l);
                if (br.containsHTML("File not found!")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                if (br.containsHTML("No connection to database")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 10 * 60 * 1000l);
                if (br.containsHTML("Aus technischen Gr") && br.containsHTML("ist ein Download momentan nicht m")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
                if (br.getURL().contains("view=error")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 10 * 60 * 1000l);
                try {
                    logger.info(br.toString());
                } catch (final Throwable e) {
                }
                try {
                    logger.info(dl.getConnection().toString());
                } catch (final Throwable e) {
                }
                if ("No htmlCode read".equalsIgnoreCase(br.toString())) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (dl.getConnection().getResponseCode() == 404) {
                br.followConnection();
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            dl.startDownload();
        }
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasAutoCaptcha() {
        return true;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    private void login(Account account) throws Exception {
        this.setBrowserExclusive();
        workAroundTimeOut(br);
        br.setDebug(true);
        br.setFollowRedirects(true);
        br.setAcceptLanguage("en, en-gb;q=0.8");
        br.setCookie("http://uploaded.to", "lang", "en");
        br.getPage("http://uploaded.to");
        br.getPage("http://uploaded.to/language/en");
        br.postPage("http://uploaded.to/io/login", "id=" + Encoding.urlEncode(account.getUser()) + "&pw=" + Encoding.urlEncode(account.getPass()));
        if (br.containsHTML("User and password do not match")) {
            AccountInfo ai = account.getAccountInfo();
            if (ai != null) ai.setStatus("User and password do not match");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        if (br.getCookie("http://uploaded.to", "auth") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        /* workaround for free/premium issue on stable 09581 */
        return maxPrem.get();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException, InterruptedException {
        this.correctDownloadLink(downloadLink);
        String id = getID(downloadLink);
        boolean red = br.isFollowingRedirects();
        br.setFollowRedirects(false);
        try {
            br.getPage("http://uploaded.to/file/" + id + "/status");
            String ret = br.getRedirectLocation();
            if (ret != null && ret.contains("/404")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            String name = br.getRegex("(.*?)(\r|\n)").getMatch(0);
            String size = br.getRegex(".+[\r\n]{1,2}(.+)").getMatch(0);
            if (name == null || size == null) return AvailableStatus.UNCHECKABLE;
            downloadLink.setFinalFileName(name.trim());
            downloadLink.setDownloadSize(SizeFormatter.getSize(size));
        } finally {
            br.setFollowRedirects(red);
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }

}